package com.zorvyn.finance.controllers;

import com.zorvyn.finance.dto.dashboard.CategoryWiseTotalResponse;
import com.zorvyn.finance.dto.dashboard.DashboardSummaryResponse;
import com.zorvyn.finance.dto.dashboard.RecentActivityResponse;
import com.zorvyn.finance.dto.dashboard.TrendResponse;
import com.zorvyn.finance.enums.TrendGranularity;
import com.zorvyn.finance.services.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "Summary analytics endpoints for the finance dashboard.")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {
    @GetMapping("/category-wise-totals")
    @PreAuthorize("hasAuthority('ANALYTICS_READ')")
    @Operation(summary = "Get category-wise totals (income, expense, net) for scoped records")
    public List<CategoryWiseTotalResponse> categoryWiseTotals(
            @Parameter(description = "From date (inclusive)")
            @RequestParam(required = false) LocalDate from,
            @Parameter(description = "To date (inclusive)")
            @RequestParam(required = false) LocalDate to
    ) {
        return dashboardService.categoryWiseTotals(from, to);
    }

    @GetMapping("/recent-activity")
    @PreAuthorize("hasAuthority('SUMMARY_READ')")
    @Operation(summary = "Get recent financial activity for scoped records")
    public List<RecentActivityResponse> recentActivity(
            @Parameter(description = "From date (inclusive)")
            @RequestParam(required = false) LocalDate from,
            @Parameter(description = "To date (inclusive)")
            @RequestParam(required = false) LocalDate to,
            @Parameter(description = "Number of items to return (default 10, max 100)")
            @RequestParam(required = false) Integer limit
    ) {
        return dashboardService.recentActivity(from, to, limit);
    }


    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('SUMMARY_READ')")
    @Operation(summary = "Get dashboard summary (income, expense, net)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    public DashboardSummaryResponse summary(
            @Parameter(description = "From date (inclusive)") @RequestParam LocalDate from,
            @Parameter(description = "To date (inclusive)") @RequestParam LocalDate to
    ) {
        return dashboardService.summary(from, to);
    }

    @GetMapping("/trends")
    @PreAuthorize("hasAuthority('ANALYTICS_READ')")
    @Operation(
            summary = "Get income/expense/net trends grouped by granularity",
            description = """
                    Returns trend data bucketed by the specified granularity over the given date range.

                    **Granularity formats:**
                    - `DAILY`   → period: `"2025-04-01"`
                    - `WEEKLY`  → period: `"2025-W14"` (ISO 8601, week starts Monday)
                    - `MONTHLY` → period: `"2025-04"`

                    Each bucket includes `periodStart` and `periodEnd` so frontend chart
                    libraries can plot the X-axis without parsing the period string.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid granularity or date params", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    public List<TrendResponse> trends(
            @Parameter(description = "From date (inclusive)") @RequestParam LocalDate from,
            @Parameter(description = "To date (inclusive)") @RequestParam LocalDate to,
            @Parameter(description = "Aggregation granularity: DAILY, WEEKLY, or MONTHLY")
            @RequestParam(defaultValue = "DAILY") TrendGranularity granularity
    ) {
        return dashboardService.trends(from, to, granularity);
    }
}

