package com.zorvyn.finance.controllers;

import com.zorvyn.finance.dto.dashboard.CategoryWiseTotalResponse;
import com.zorvyn.finance.dto.dashboard.DailyTrendResponse;
import com.zorvyn.finance.dto.dashboard.DashboardSummaryResponse;
import com.zorvyn.finance.dto.dashboard.RecentActivityResponse;
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

    @GetMapping("/trends/daily")
    @PreAuthorize("hasAuthority('ANALYTICS_READ')")
    @Operation(summary = "Get daily trend (both income and expense) over a date range")
    public List<DailyTrendResponse> dailyTrend(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        return dashboardService.dailyTrend(from, to);
    }
}

