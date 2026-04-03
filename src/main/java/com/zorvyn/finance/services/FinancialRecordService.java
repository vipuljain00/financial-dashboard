package com.zorvyn.finance.services;

import com.zorvyn.finance.dto.records.FinancialRecordCreateRequest;
import com.zorvyn.finance.dto.records.FinancialRecordFilter;
import com.zorvyn.finance.dto.records.FinancialRecordResponse;
import com.zorvyn.finance.dto.records.FinancialRecordUpdateRequest;
import com.zorvyn.finance.entities.FinancialRecord;
import com.zorvyn.finance.entities.User;
import com.zorvyn.finance.enums.EntityStatus;
import com.zorvyn.finance.enums.Role;
import com.zorvyn.finance.exceptions.ForbiddenException;
import com.zorvyn.finance.exceptions.NotFoundException;
import com.zorvyn.finance.repositories.FinancialRecordRepository;
import com.zorvyn.finance.repositories.FinancialRecordSpecifications;
import com.zorvyn.finance.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FinancialRecordService {

    private final FinancialRecordRepository repository;
    private final CurrentUserService currentUserService;

    @Transactional
    public FinancialRecordResponse createRecord(FinancialRecordCreateRequest request) {
        User user = currentUserService.getCurrentUser();

        FinancialRecord record = new FinancialRecord();
        record.setAmount(request.amount());
        record.setType(request.type());
        record.setCategory(request.category());
        record.setTransactionDate(request.transactionDate());
        record.setNotes(request.notes());
        record.setCreatedBy(user);
        record.setStatus(EntityStatus.ACTIVE);

        FinancialRecord saved = repository.save(record);
        return toResponse(saved);
    }

    @Transactional
    public FinancialRecordResponse updateRecord(Long id, FinancialRecordUpdateRequest updated) {
        User user = currentUserService.getCurrentUser();

        FinancialRecord record = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Record not found"));

        if (record.getCreatedBy() == null || record.getCreatedBy().getId() == null) {
            throw new ForbiddenException("Record owner information is missing");
        }

        if (!record.getCreatedBy().getId().equals(user.getId())) {
            throw new ForbiddenException("You can only update records created by you");
        }

        record.setAmount(updated.amount());
        record.setCategory(updated.category());
        record.setType(updated.type());
        record.setTransactionDate(updated.transactionDate());
        record.setNotes(updated.notes());

        return toResponse(repository.save(record));
    }

    @Transactional
    public void deleteRecord(Long id) {
        User user = currentUserService.getCurrentUser();

        FinancialRecord record = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Record not found"));

        if (record.getCreatedBy() == null || record.getCreatedBy().getId() == null) {
            throw new ForbiddenException("Record owner information is missing");
        }

        if (!record.getCreatedBy().getId().equals(user.getId())) {
            throw new ForbiddenException("You can only delete records created by you");
        }

        // Permanent delete from database
        repository.delete(record);
    }

    @Transactional(readOnly = true)
    public FinancialRecordResponse getRecord(Long id) {
        User user = currentUserService.getCurrentUser();

        FinancialRecord record = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Record not found"));

        if (record.getCreatedBy() == null || record.getCreatedBy().getId() == null) {
            throw new ForbiddenException("Record owner information is missing");
        }

        Long creatorId = record.getCreatedBy().getId();

        if (user.getRole() == Role.ADMIN) {
            if (!creatorId.equals(user.getId())) {
                throw new ForbiddenException("Admins can only view records created by themselves");
            }
            return toResponse(record);
        }

        User admin = user.getAdmin();
        if (admin == null || admin.getId() == null) {
            throw new ForbiddenException("User is not assigned to an admin, cannot view records");
        }

        if (!creatorId.equals(admin.getId())) {
            throw new ForbiddenException("You can only view records created by your admin");
        }

        return toResponse(record);
    }

    @Transactional(readOnly = true)
    public Page<FinancialRecordResponse> listRecords(FinancialRecordFilter filter, Pageable pageable) {
        User user = currentUserService.getCurrentUser();

        // Visibility:
        // - Admin: only records created by themselves
        // - Analyst: records created by their admin
        Long createdById;
        if (user.getRole() == Role.ADMIN) {
            createdById = user.getId();
        } else {
            User admin = user.getAdmin();
            if (admin == null || admin.getId() == null) {
                throw new ForbiddenException("User is not assigned to an admin, cannot list records");
            }
            createdById = admin.getId();
        }

        var spec = FinancialRecordSpecifications.hasType(filter.type())
                .and(FinancialRecordSpecifications.categoryContains(filter.category()))
                .and(FinancialRecordSpecifications.transactionDateGte(filter.fromDate()))
                .and(FinancialRecordSpecifications.transactionDateLte(filter.toDate()))
                .and(FinancialRecordSpecifications.amountGte(filter.minAmount()))
                .and(FinancialRecordSpecifications.amountLte(filter.maxAmount()))
                .and(FinancialRecordSpecifications.createdByUserId(createdById));

        return repository.findAll(spec, pageable).map(this::toResponse);
    }

    private FinancialRecordResponse toResponse(FinancialRecord record) {
        Long createdById = record.getCreatedBy() == null ? null : record.getCreatedBy().getId();
        return new FinancialRecordResponse(
                record.getId(),
                record.getAmount(),
                record.getType(),
                record.getCategory(),
                record.getTransactionDate(),
                record.getNotes(),
                createdById,
                record.getStatus(),
                record.getDateCreated(),
                record.getDateUpdated());
    }
}
