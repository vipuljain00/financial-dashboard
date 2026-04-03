package com.zorvyn.finance.services;

import com.zorvyn.finance.dto.dashboard.CategoryWiseTotalResponse;
import com.zorvyn.finance.dto.dashboard.DailyTrendResponse;
import com.zorvyn.finance.dto.dashboard.DashboardSummaryResponse;
import com.zorvyn.finance.dto.dashboard.RecentActivityResponse;
import com.zorvyn.finance.entities.FinancialRecord;
import com.zorvyn.finance.entities.User;
import com.zorvyn.finance.enums.Role;
import com.zorvyn.finance.enums.RecordType;
import com.zorvyn.finance.exceptions.ForbiddenException;
import com.zorvyn.finance.repositories.FinancialRecordRepository;
import com.zorvyn.finance.repositories.FinancialRecordSpecifications;
import com.zorvyn.finance.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FinancialRecordRepository financialRecordRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<CategoryWiseTotalResponse> categoryWiseTotals(LocalDate from, LocalDate to) {
        Long creatorId = resolveScopedCreatorId();
        var spec = FinancialRecordSpecifications.createdByUserId(creatorId)
                .and(FinancialRecordSpecifications.transactionDateGte(normalizeFrom(from)))
                .and(FinancialRecordSpecifications.transactionDateLte(normalizeTo(to)));

        List<FinancialRecord> records = financialRecordRepository.findAll(spec);
        Map<String, BigDecimal> incomeByCategory = new LinkedHashMap<>();
        Map<String, BigDecimal> expenseByCategory = new LinkedHashMap<>();

        for (FinancialRecord r : records) {
            String category = r.getCategory();
            if (r.getType() == RecordType.INCOME) {
                incomeByCategory.put(category, incomeByCategory.getOrDefault(category, BigDecimal.ZERO).add(r.getAmount()));
            } else if (r.getType() == RecordType.EXPENSE) {
                expenseByCategory.put(category, expenseByCategory.getOrDefault(category, BigDecimal.ZERO).add(r.getAmount()));
            }
        }

        // merge keys preserving first-seen order from source maps
        LinkedHashMap<String, Boolean> categories = new LinkedHashMap<>();
        incomeByCategory.keySet().forEach(k -> categories.put(k, true));
        expenseByCategory.keySet().forEach(k -> categories.put(k, true));

        return categories.keySet().stream()
                .map(category -> {
                    BigDecimal income = incomeByCategory.getOrDefault(category, BigDecimal.ZERO);
                    BigDecimal expense = expenseByCategory.getOrDefault(category, BigDecimal.ZERO);
                    return new CategoryWiseTotalResponse(category, income, expense, income.subtract(expense));
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RecentActivityResponse> recentActivity(LocalDate from, LocalDate to, Integer limit) {
        Long creatorId = resolveScopedCreatorId();
        int safeLimit = (limit == null || limit <= 0) ? 10 : Math.min(limit, 100);
        var spec = FinancialRecordSpecifications.createdByUserId(creatorId)
                .and(FinancialRecordSpecifications.transactionDateGte(normalizeFrom(from)))
                .and(FinancialRecordSpecifications.transactionDateLte(normalizeTo(to)));

        List<FinancialRecord> rows = financialRecordRepository.findAll(
                spec,
                PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "dateUpdated"))
        ).getContent();

        return rows.stream()
                .map(this::toRecentActivity)
                .toList();
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse summary(LocalDate from, LocalDate to) {
        Long creatorId = resolveScopedCreatorId();
        BigDecimal income = totalByType(RecordType.INCOME, creatorId, from, to);
        BigDecimal expense = totalByType(RecordType.EXPENSE, creatorId, from, to);
        BigDecimal net = income.subtract(expense);
        return new DashboardSummaryResponse(income, expense, net);
    }

    @Transactional(readOnly = true)
    public List<DailyTrendResponse> dailyTrend(LocalDate from, LocalDate to) {
        Long creatorId = resolveScopedCreatorId();
        var spec = FinancialRecordSpecifications.createdByUserId(creatorId)
                .and(FinancialRecordSpecifications.transactionDateGte(normalizeFrom(from)))
                .and(FinancialRecordSpecifications.transactionDateLte(normalizeTo(to)));

        List<FinancialRecord> records = financialRecordRepository.findAll(spec);
        Map<LocalDate, BigDecimal> incomeByDate = new TreeMap<>();
        Map<LocalDate, BigDecimal> expenseByDate = new TreeMap<>();

        for (FinancialRecord r : records) {
            LocalDate d = r.getTransactionDate();
            if (r.getType() == RecordType.INCOME) {
                incomeByDate.put(d, incomeByDate.getOrDefault(d, BigDecimal.ZERO).add(r.getAmount()));
            } else if (r.getType() == RecordType.EXPENSE) {
                expenseByDate.put(d, expenseByDate.getOrDefault(d, BigDecimal.ZERO).add(r.getAmount()));
            }
        }

        TreeMap<LocalDate, Boolean> dates = new TreeMap<>();
        incomeByDate.keySet().forEach(k -> dates.put(k, true));
        expenseByDate.keySet().forEach(k -> dates.put(k, true));

        return dates.keySet().stream()
                .map(d -> new DailyTrendResponse(
                        d,
                        incomeByDate.getOrDefault(d, BigDecimal.ZERO),
                        expenseByDate.getOrDefault(d, BigDecimal.ZERO)
                ))
                .toList();
    }

    private BigDecimal totalByType(RecordType type, Long creatorId, LocalDate from, LocalDate to) {
        return financialRecordRepository.totalByTypeForUser(type, creatorId, from, to);
    }

    private LocalDate normalizeFrom(LocalDate from) {
        return from != null ? from : LocalDate.of(1970, 1, 1);
    }

    private LocalDate normalizeTo(LocalDate to) {
        return to != null ? to : LocalDate.now();
    }

    private Long resolveScopedCreatorId() {
        User user = currentUserService.getCurrentUser();
        if (user.getRole() == Role.ADMIN) {
            return user.getId();
        }

        User admin = user.getAdmin();
        if (admin == null || admin.getId() == null) {
            throw new ForbiddenException("User is not assigned to an admin");
        }
        return admin.getId();
    }

    private RecentActivityResponse toRecentActivity(FinancialRecord record) {
        OffsetDateTime created = record.getDateCreated();
        OffsetDateTime updated = record.getDateUpdated();
        String action = (created != null && updated != null && updated.isAfter(created)) ? "UPDATED" : "CREATED";

        return new RecentActivityResponse(
                record.getId(),
                action,
                record.getAmount(),
                record.getType(),
                record.getCategory(),
                updated != null ? updated : created,
                record.getCreatedBy() == null ? null : record.getCreatedBy().getId()
        );
    }
}

