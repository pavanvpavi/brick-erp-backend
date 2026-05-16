package com.brickerp.reports.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CustomerSalesReportResponse {
    private Long customerId;
    private String customerName;
    private String customerCode;
    private String period;
    private Long totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal totalPaid;
    private BigDecimal totalOutstanding;
    private Long totalItemsPurchased;
    private List<CustomerOrderEntry> orders;
    private List<TopProductEntry> topProducts;

    @Data
    @Builder
    public static class CustomerOrderEntry {
        private String orderNumber;
        private String orderDate;
        private String status;
        private Integer itemCount;
        private BigDecimal totalAmount;
        private String invoiceNumber;
        private String invoiceStatus;
    }

    @Data
    @Builder
    public static class TopProductEntry {
        private String productName;
        private String productSku;
        private Long quantityPurchased;
        private BigDecimal totalAmount;
    }
}