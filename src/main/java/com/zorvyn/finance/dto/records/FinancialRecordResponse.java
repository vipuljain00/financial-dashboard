package com.zorvyn.finance.dto.records;

import com.zorvyn.finance.enums.EntityStatus;
import com.zorvyn.finance.enums.RecordType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Schema(name = "FinancialRecordResponse")
public record FinancialRecordResponse(
        Long id,
        BigDecimal amount,
        RecordType type,
        String category,
        LocalDate transactionDate,
        String notes,
        Long createdByUserId,
        EntityStatus status,
        OffsetDateTime dateCreated,
        OffsetDateTime dateUpdated
) {
}

