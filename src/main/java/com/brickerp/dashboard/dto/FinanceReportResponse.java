package com.brickerp.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceReportResponse {
    private BigDecimal totalInvoiced;
    private BigDecimal totalCollected;
    private BigDecimal totalOutstanding;
    private Long paidInvoices;
    private Long unpaidInvoices;
    private Long overdueInvoices;
    private List<OutstandingInvoiceDto> outstandingInvoices;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OutstandingInvoiceDto {
        private String invoiceNumber;
        private String customerName;
        private BigDecimal totalAmount;
        private BigDecimal balanceDue;
        private String dueDate;
        private String status;
    }
}