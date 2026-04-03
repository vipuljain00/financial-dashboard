package com.zorvyn.finance.dto.records;

import com.zorvyn.finance.enums.RecordType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(name = "FinancialRecordFilter")
public record FinancialRecordFilter(
        RecordType type,
        String category,
        LocalDate fromDate,
        LocalDate toDate,
        BigDecimal minAmount,
        BigDecimal maxAmount
) {
}

