package com.brickerp.reports.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ProductSalesReportResponse {
    private Long productId;
    private String productName;
    private String productSku;
    private String period;
    private Long totalOrdersContaining;
    private Long totalQuantitySold;
    private BigDecimal totalRevenue;
    private BigDecimal averageSellingPrice;
    private BigDecimal costPrice;
    private BigDecimal grossProfit;
    private List<ProductSaleEntry> salesHistory;
    private List<MonthlySalesSummary> monthlySummary;

    @Data
    @Builder
    public static class ProductSaleEntry {
        private String orderNumber;
        private String orderDate;
        private String customerName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal discountAmount;
        private BigDecimal lineTotal;
    }

    @Data
    @Builder
    public static class MonthlySalesSummary {
        private String month;
        private Long quantitySold;
        private BigDecimal revenue;
    }
}