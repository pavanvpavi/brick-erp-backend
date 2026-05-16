package com.brickerp.reports.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ProfitLossReportResponse {
    private String period;

    // Revenue
    private BigDecimal totalRevenue;
    private BigDecimal totalTaxCollected;
    private BigDecimal totalDiscountGiven;
    private BigDecimal netRevenue;

    // Cost of Goods Sold
    private BigDecimal costOfGoodsSold;
    private BigDecimal grossProfit;
    private BigDecimal grossProfitMargin;

    // Expenses
    private BigDecimal totalExpenses;
    private List<ExpenseCategorySummary> expenseBreakdown;

    // Net Profit
    private BigDecimal netProfit;
    private BigDecimal netProfitMargin;

    // Collections
    private BigDecimal totalInvoiced;
    private BigDecimal totalCollected;
    private BigDecimal totalOutstanding;

    @Data
    @Builder
    public static class ExpenseCategorySummary {
        private String category;
        private BigDecimal amount;
        private Double percentage;
    }
}