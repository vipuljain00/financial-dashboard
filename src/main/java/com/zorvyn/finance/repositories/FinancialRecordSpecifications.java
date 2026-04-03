package com.zorvyn.finance.repositories;

import com.zorvyn.finance.entities.FinancialRecord;
import com.zorvyn.finance.enums.Role;
import com.zorvyn.finance.enums.RecordType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class FinancialRecordSpecifications {
    private FinancialRecordSpecifications() {
    }

    public static Specification<FinancialRecord> hasType(RecordType type) {
        return (root, query, cb) -> type == null ? cb.conjunction() : cb.equal(root.get("type"), type);
    }

    public static Specification<FinancialRecord> categoryContains(String category) {
        return (root, query, cb) -> {
            if (category == null || category.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("category")), "%" + category.toLowerCase() + "%");
        };
    }

    public static Specification<FinancialRecord> transactionDateGte(LocalDate from) {
        return (root, query, cb) -> from == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("transactionDate"), from);
    }

    public static Specification<FinancialRecord> transactionDateLte(LocalDate to) {
        return (root, query, cb) -> to == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("transactionDate"), to);
    }

    public static Specification<FinancialRecord> amountGte(BigDecimal min) {
        return (root, query, cb) -> min == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("amount"), min);
    }

    public static Specification<FinancialRecord> amountLte(BigDecimal max) {
        return (root, query, cb) -> max == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("amount"), max);
    }

    public static Specification<FinancialRecord> createdByUserId(Long userId) {
        return (root, query, cb) -> userId == null ? cb.conjunction() : cb.equal(root.get("createdBy").get("id"), userId);
    }

    public static Specification<FinancialRecord> createdByRole(Role role) {
        return (root, query, cb) -> role == null
                ? cb.conjunction()
                : cb.equal(root.get("createdBy").get("role"), role);
    }
}

