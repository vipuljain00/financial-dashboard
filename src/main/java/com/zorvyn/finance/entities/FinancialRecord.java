package com.zorvyn.finance.entities;

import com.zorvyn.finance.enums.RecordType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "financial_record",
        indexes = {
                @Index(name = "idx_record_date", columnList = "transactionDate"),
                @Index(name = "idx_record_type", columnList = "type"),
                @Index(name = "idx_record_category", columnList = "category"),
                @Index(name = "idx_record_user", columnList = "created_by")
        }
)
@Getter
@Setter
public class FinancialRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @DecimalMin("0.01")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecordType type;

    @NotBlank
    @Column(nullable = false, length = 80)
    private String category;

    @PastOrPresent
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(length = 500)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
}