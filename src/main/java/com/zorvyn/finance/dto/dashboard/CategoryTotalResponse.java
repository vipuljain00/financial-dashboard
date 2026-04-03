package com.zorvyn.finance.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "CategoryTotalResponse")
public record CategoryTotalResponse(
        String category,
        BigDecimal totalAmount
) {
}

