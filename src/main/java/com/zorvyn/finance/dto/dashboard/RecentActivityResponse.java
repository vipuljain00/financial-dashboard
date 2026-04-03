package com.zorvyn.finance.dto.dashboard;

import com.zorvyn.finance.enums.RecordType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Schema(name = "RecentActivityResponse")
public record RecentActivityResponse(
        Long recordId,
        String action,
        BigDecimal amount,
        RecordType type,
        String category,
        OffsetDateTime activityAt,
        Long createdById
) {
}

