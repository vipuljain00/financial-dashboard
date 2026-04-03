package com.zorvyn.finance.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(name = "TrendResponse")
public record TrendResponse(
        @Schema(description = "Period label: '2025-04-01' (DAILY), '2025-W14' (WEEKLY), '2025-04' (MONTHLY)")
        String period,
        @Schema(description = "Start date of this period (inclusive)")
        LocalDate periodStart,
        @Schema(description = "End date of this period (inclusive)")
        LocalDate periodEnd,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netBalance
) {}
