package com.zorvyn.finance.controllers;

import com.zorvyn.finance.dto.common.PagedResponse;
import com.zorvyn.finance.dto.records.FinancialRecordCreateRequest;
import com.zorvyn.finance.dto.records.FinancialRecordFilter;
import com.zorvyn.finance.dto.records.FinancialRecordResponse;
import com.zorvyn.finance.dto.records.FinancialRecordUpdateRequest;
import com.zorvyn.finance.enums.RecordType;
import com.zorvyn.finance.services.FinancialRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/records")
@Tag(name = "Financial Records", description = "Admin-created financial records with role-based visibility and filtering.")
@SecurityRequirement(name = "bearerAuth")
public class FinancialRecordController {

    private final FinancialRecordService financialRecordService;

    public FinancialRecordController(FinancialRecordService financialRecordService) {
        this.financialRecordService = financialRecordService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a financial record (ADMIN only, createdBy set to current admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    public ResponseEntity<FinancialRecordResponse> create(@Valid @RequestBody FinancialRecordCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(financialRecordService.createRecord(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a record by id")
    public FinancialRecordResponse get(@PathVariable Long id) {
        return financialRecordService.getRecord(id);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get records with filters (ADMIN: own records; ANALYST: records created by their admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = FinancialRecordResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    public PagedResponse<FinancialRecordResponse> list(
            @Parameter(description = "Record type filter") @RequestParam(required = false) RecordType type,
            @Parameter(description = "Category substring filter") @RequestParam(required = false) String category,
            @Parameter(description = "From date (inclusive)") @RequestParam(required = false) LocalDate fromDate,
            @Parameter(description = "To date (inclusive)") @RequestParam(required = false) LocalDate toDate,
            @Parameter(description = "Minimum amount") @RequestParam(required = false) BigDecimal minAmount,
            @Parameter(description = "Maximum amount") @RequestParam(required = false) BigDecimal maxAmount,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable
    ) {
        FinancialRecordFilter filter = new FinancialRecordFilter(type, category, fromDate, toDate, minAmount, maxAmount);
        return PagedResponse.fromPage(financialRecordService.listRecords(filter, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update a financial record by id (ADMIN can only update their own records)")
    public FinancialRecordResponse update(@PathVariable Long id, @Valid @RequestBody FinancialRecordUpdateRequest request) {
        return financialRecordService.updateRecord(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete a financial record by id (permanent delete, ADMIN can only delete their own records)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    public ResponseEntity<?> delete(@PathVariable Long id) {
        financialRecordService.deleteRecord(id);
        return ResponseEntity.ok(
                java.util.Map.of("message", "Record deleted successfully")
        );
    }
}

