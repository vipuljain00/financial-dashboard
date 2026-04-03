package com.zorvyn.finance.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "TotalAmountResponse")
public record TotalAmountResponse(
        BigDecimal total
) {
}

