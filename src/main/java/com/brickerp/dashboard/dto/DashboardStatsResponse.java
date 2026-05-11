package com.brickerp.dashboard.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

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
    private java.math.BigDecimal totalExpensesThisMonth;
}