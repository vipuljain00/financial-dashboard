package com.zorvyn.finance.dto.records;

import com.zorvyn.finance.enums.RecordType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(name = "FinancialRecordUpdateRequest")
public record FinancialRecordUpdateRequest(
        @Schema(example = "1250.50")
        @NotNull
        @DecimalMin("0.01")
        BigDecimal amount,

        @NotNull
        RecordType type,

        @NotBlank
        @Size(max = 80)
        String category,

        @NotNull
        @PastOrPresent
        LocalDate transactionDate,

        @Size(max = 500)
        String notes
) {
}

