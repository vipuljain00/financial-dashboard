package com.zorvyn.finance.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "CategoryWiseTotalResponse")
public record CategoryWiseTotalResponse(
        String category,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netBalance
) {
}

