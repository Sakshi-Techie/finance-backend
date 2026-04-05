package com.zorvyn.finance.service;

import com.zorvyn.finance.dto.DashboardSummary;
import com.zorvyn.finance.dto.FinancialRecordResponse;
import com.zorvyn.finance.model.TransactionType;
import com.zorvyn.finance.repository.FinancialRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final FinancialRecordRepository recordRepository;

    @Transactional(readOnly = true)
    public DashboardSummary getDashboardSummary() {
        BigDecimal totalIncome = recordRepository.getTotalIncome();
        BigDecimal totalExpenses = recordRepository.getTotalExpenses();
        BigDecimal netBalance = totalIncome.subtract(totalExpenses);

        Map<String, BigDecimal> categoryWiseTotals = buildCategoryWiseTotals();

        List<FinancialRecordResponse> recentActivity = recordRepository
                .findRecentActivity(PageRequest.of(0, 10))
                .stream()
                .map(FinancialRecordResponse::fromRecord)
                .collect(Collectors.toList());

        List<DashboardSummary.MonthlyTrend> monthlyTrends = buildMonthlyTrends();

        return DashboardSummary.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(netBalance)
                .categoryWiseTotals(categoryWiseTotals)
                .recentActivity(recentActivity)
                .monthlyTrends(monthlyTrends)
                .build();
    }

    private Map<String, BigDecimal> buildCategoryWiseTotals() {
        List<Object[]> rawData = recordRepository.getCategoryWiseTotals();
        Map<String, BigDecimal> totals = new LinkedHashMap<>();

        for (Object[] row : rawData) {
            String category = (String) row[0];
            TransactionType type = TransactionType.valueOf(row[1].toString());
            BigDecimal amount = (BigDecimal) row[2];

            String key = category + " (" + type.name() + ")";
            totals.put(key, amount);
        }
        return totals;
    }

    private List<DashboardSummary.MonthlyTrend> buildMonthlyTrends() {
        List<Object[]> rawData = recordRepository.getMonthlyTrends();

        // Group by year+month
        Map<String, Map<String, BigDecimal>> grouped = new LinkedHashMap<>();

        for (Object[] row : rawData) {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            TransactionType type = TransactionType.valueOf(row[2].toString());
            BigDecimal amount = (BigDecimal) row[3];

            String key = year + "-" + String.format("%02d", month);
            grouped.computeIfAbsent(key, k -> new HashMap<>());
            grouped.get(key).put(type.name(), amount);
            grouped.get(key).put("YEAR", BigDecimal.valueOf(year));
            grouped.get(key).put("MONTH", BigDecimal.valueOf(month));
        }

        List<DashboardSummary.MonthlyTrend> trends = new ArrayList<>();
        for (Map.Entry<String, Map<String, BigDecimal>> entry : grouped.entrySet()) {
            Map<String, BigDecimal> data = entry.getValue();
            int year = data.getOrDefault("YEAR", BigDecimal.ZERO).intValue();
            int month = data.getOrDefault("MONTH", BigDecimal.ZERO).intValue();
            BigDecimal income = data.getOrDefault("INCOME", BigDecimal.ZERO);
            BigDecimal expenses = data.getOrDefault("EXPENSE", BigDecimal.ZERO);

            trends.add(DashboardSummary.MonthlyTrend.builder()
                    .year(year)
                    .month(month)
                    .monthName(Month.of(month).name())
                    .income(income)
                    .expenses(expenses)
                    .net(income.subtract(expenses))
                    .build());
        }
        return trends;
    }
}
