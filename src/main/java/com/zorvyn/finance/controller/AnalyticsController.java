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

import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final DashboardService dashboardService;

    /**
     * GET /api/analytics/insights
     * Returns analytical insights (monthly trends and category breakdowns).
     * Access: ANALYST, ADMIN only
     */
    @GetMapping("/insights")
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getInsights() {
        DashboardSummary summary = dashboardService.getDashboardSummary();

        Map<String, Object> insights = Map.of(
                "monthlyTrends", summary.getMonthlyTrends(),
                "categoryWiseTotals", summary.getCategoryWiseTotals(),
                "totalIncome", summary.getTotalIncome(),
                "totalExpenses", summary.getTotalExpenses(),
                "netBalance", summary.getNetBalance()
        );

        return ResponseEntity.ok(ApiResponse.success(insights, "Analytics insights retrieved successfully"));
    }
}
