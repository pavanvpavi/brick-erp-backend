package com.brickerp.reports.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class SalesReportDetailResponse {
    private String period;
    private Long totalOrders;
    private Long confirmedOrders;
    private Long cancelledOrders;
    private BigDecimal totalRevenue;
    private BigDecimal totalTax;
    private BigDecimal totalDiscount;
    private BigDecimal netRevenue;
    private Long totalItemsSold;
    private List<SalesOrderEntry> orders;
    private List<MonthlySummary> monthlySummary;

    @Data
    @Builder
    public static class SalesOrderEntry {
        private String orderNumber;
        private String orderDate;
        private String customerName;
        private String status;
        private Integer itemCount;
        private BigDecimal subtotal;
        private BigDecimal taxAmount;
        private BigDecimal discountAmount;
        private BigDecimal totalAmount;
    }

    @Data
    @Builder
    public static class MonthlySummary {
        private String month;
        private Long orderCount;
        private BigDecimal revenue;
        private Long itemsSold;
    }
}