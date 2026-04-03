package com.zorvyn.finance.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(name = "DailyTrendResponse")
public record DailyTrendResponse(
        LocalDate date,
        BigDecimal totalIncome,
        BigDecimal totalExpense
) {
}

