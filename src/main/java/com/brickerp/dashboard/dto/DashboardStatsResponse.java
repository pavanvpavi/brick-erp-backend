package com.brickerp.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DashboardStatsResponse {

    // Sales KPIs
    private Long totalOrders;
    private Long pendingOrders;
    private Long confirmedOrders;
    private BigDecimal totalSalesAmount;
    private BigDecimal totalSalesThisMonth;

    // Inventory KPIs
    private Long totalProducts;
    private Long lowStockItems;
    private Long totalWarehouses;

    // Finance KPIs
    private Long totalInvoices;
    private Long unpaidInvoices;
    private BigDecimal totalOutstanding;
    private BigDecimal totalCollected;

    // Procurement KPIs
    private Long totalSuppliers;
    private Long pendingPurchaseOrders;

    // Manufacturing KPIs
    private Long activeProductionOrders;
    private Long completedProductionOrders;

    // Customer KPIs
    private Long totalCustomers;
    private Long activeCustomers;

    private Long pendingDeliveries;
    private Long totalQualityTests;
    private Double averagePassRate;
    private BigDecimal totalExpensesThisMonth;

    private List<MonthlyRevenueData> monthlyRevenue;
    private List<TopProductData> topProducts;
    private List<StockLevelData> stockLevels;

    @Data
    @Builder
    public static class MonthlyRevenueData {
        private String month;
        private Double revenue;
        private Long orders;
    }

    @Data
    @Builder
    public static class TopProductData {
        private String name;
        private Long quantity;
        private Double revenue;
    }

    @Data
    @Builder
    public static class StockLevelData {
        private String product;
        private Integer current;
        private Integer minimum;
    }
}