package com.zorvyn.finance.controller;

import com.zorvyn.finance.dto.ApiResponse;
import com.zorvyn.finance.dto.DashboardSummary;
import com.zorvyn.finance.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * GET /api/dashboard/summary
     * Returns complete dashboard summary:
     *   - Total income, total expenses, net balance
     *   - Category-wise totals
     *   - Recent activity (last 10 records)
     *   - Monthly trends
     *
     * Access: VIEWER, ANALYST, ADMIN
     */
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('VIEWER','ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<DashboardSummary>> getDashboardSummary() {
        DashboardSummary summary = dashboardService.getDashboardSummary();
        return ResponseEntity.ok(ApiResponse.success(summary, "Dashboard summary retrieved successfully"));
    }
}
