package com.zorvyn.finance.repositories;

import com.zorvyn.finance.entities.FinancialRecord;
import com.zorvyn.finance.enums.RecordType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface FinancialRecordRepository
        extends JpaRepository<FinancialRecord, Long>, JpaSpecificationExecutor<FinancialRecord> {

    @Query("""
        SELECT COALESCE(SUM(r.amount), 0)
        FROM FinancialRecord r
        WHERE r.type = :type
          AND r.status <> 'DELETED'
          AND r.createdBy.id = :userId
          AND r.transactionDate BETWEEN :from AND :to
    """)
    BigDecimal totalByTypeForUser(
            @Param("type") RecordType type,
            @Param("userId") Long userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );
}
