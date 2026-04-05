package com.zorvyn.finance.controller;

import com.zorvyn.finance.dto.ApiResponse;
import com.zorvyn.finance.dto.FinancialRecordRequest;
import com.zorvyn.finance.dto.FinancialRecordResponse;
import com.zorvyn.finance.model.TransactionType;
import com.zorvyn.finance.service.FinancialRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class FinancialRecordController {

    private final FinancialRecordService recordService;

    /**
     * GET /api/records
     * Retrieve all financial records with optional filters and pagination.
     * Access: VIEWER, ANALYST, ADMIN
     *
     * Query params:
     *   - type: INCOME | EXPENSE
     *   - category: partial match
     *   - startDate: yyyy-MM-dd
     *   - endDate: yyyy-MM-dd
     *   - page: 0-based page number (default 0)
     *   - size: page size (default 10)
     *   - sortBy: field name (default "date")
     *   - sortDir: asc | desc (default "desc")
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('VIEWER','ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<Page<FinancialRecordResponse>>> getAllRecords(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Page<FinancialRecordResponse> records =
                recordService.getAllRecords(type, category, startDate, endDate, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    /**
     * GET /api/records/{id}
     * Get a single financial record by ID.
     * Access: VIEWER, ANALYST, ADMIN
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('VIEWER','ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<FinancialRecordResponse>> getRecordById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(recordService.getRecordById(id)));
    }

    /**
     * POST /api/records
     * Create a new financial record.
     * Access: ADMIN only
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FinancialRecordResponse>> createRecord(
            @Valid @RequestBody FinancialRecordRequest request) {
        FinancialRecordResponse response = recordService.createRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Financial record created successfully"));
    }

    /**
     * PUT /api/records/{id}
     * Update an existing financial record.
     * Access: ADMIN only
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FinancialRecordResponse>> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody FinancialRecordRequest request) {
        FinancialRecordResponse response = recordService.updateRecord(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Financial record updated successfully"));
    }

    /**
     * DELETE /api/records/{id}
     * Soft-delete a financial record.
     * Access: ADMIN only
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRecord(@PathVariable Long id) {
        recordService.deleteRecord(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Financial record deleted successfully"));
    }
}
