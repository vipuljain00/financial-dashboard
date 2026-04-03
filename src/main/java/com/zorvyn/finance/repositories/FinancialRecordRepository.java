package com.zorvyn.finance.repositories;

import com.zorvyn.finance.entities.FinancialRecord;
import com.zorvyn.finance.enums.RecordType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface FinancialRecordRepository
        extends JpaRepository<FinancialRecord, Long>, JpaSpecificationExecutor<FinancialRecord> {

    // Dashboard queries

    @Query("""
        SELECT COALESCE(SUM(r.amount), 0)
        FROM FinancialRecord r
        WHERE r.type = :type
          AND r.status <> 'DELETED'
          AND r.transactionDate BETWEEN :from AND :to
    """)
    BigDecimal totalByTypeAllUsers(
            @Param("type") RecordType type,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

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

    @Query("""
        SELECT r.category, COALESCE(SUM(r.amount), 0)
        FROM FinancialRecord r
        WHERE r.status <> 'DELETED'
          AND r.transactionDate BETWEEN :from AND :to
        GROUP BY r.category
    """)
    List<Object[]> categoryTotalsAllUsers(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("""
        SELECT r.category, COALESCE(SUM(r.amount), 0)
        FROM FinancialRecord r
        WHERE r.status <> 'DELETED'
          AND r.createdBy.id = :userId
          AND r.transactionDate BETWEEN :from AND :to
        GROUP BY r.category
    """)
    List<Object[]> categoryTotalsForUser(
            @Param("userId") Long userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("""
        SELECT r.transactionDate, COALESCE(SUM(r.amount), 0)
        FROM FinancialRecord r
        WHERE r.status <> 'DELETED'
          AND r.type = :type
          AND r.transactionDate BETWEEN :from AND :to
        GROUP BY r.transactionDate
        ORDER BY r.transactionDate
    """)
    List<Object[]> dailyTrendAllUsers(
            @Param("type") RecordType type,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("""
        SELECT r.transactionDate, COALESCE(SUM(r.amount), 0)
        FROM FinancialRecord r
        WHERE r.status <> 'DELETED'
          AND r.type = :type
          AND r.createdBy.id = :userId
          AND r.transactionDate BETWEEN :from AND :to
        GROUP BY r.transactionDate
        ORDER BY r.transactionDate
    """)
    List<Object[]> dailyTrendForUser(
            @Param("type") RecordType type,
            @Param("userId") Long userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("""
        SELECT COALESCE(SUM(r.amount), 0)
        FROM FinancialRecord r
        WHERE r.createdBy.id = :creatorId
          AND (:type IS NULL OR r.type = :type)
          AND (:category IS NULL OR LOWER(r.category) LIKE LOWER(CONCAT('%', :category, '%')))
          AND (:from IS NULL OR r.transactionDate >= :from)
          AND (:to IS NULL OR r.transactionDate <= :to)
    """)
    BigDecimal sumAmountForCreator(
            @Param("creatorId") Long creatorId,
            @Param("type") RecordType type,
            @Param("category") String category,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("""
        SELECT r.category,
               COALESCE(SUM(CASE WHEN r.type = 'INCOME' THEN r.amount ELSE 0 END), 0),
               COALESCE(SUM(CASE WHEN r.type = 'EXPENSE' THEN r.amount ELSE 0 END), 0)
        FROM FinancialRecord r
        WHERE r.createdBy.id = :creatorId
          AND (:from IS NULL OR r.transactionDate >= :from)
          AND (:to IS NULL OR r.transactionDate <= :to)
        GROUP BY r.category
        ORDER BY r.category ASC
    """)
    List<Object[]> categoryWiseTotalsForCreator(
            @Param("creatorId") Long creatorId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("""
        SELECT r
        FROM FinancialRecord r
        WHERE r.createdBy.id = :creatorId
          AND (:from IS NULL OR r.transactionDate >= :from)
          AND (:to IS NULL OR r.transactionDate <= :to)
        ORDER BY r.dateUpdated DESC
    """)
    List<FinancialRecord> recentActivityForCreator(
            @Param("creatorId") Long creatorId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            Pageable pageable
    );
}
