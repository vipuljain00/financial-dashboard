package com.zorvyn.finance.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(name = "TrendPointResponse")
public record TrendPointResponse(
        LocalDate date,
        BigDecimal totalAmount
) {
}

