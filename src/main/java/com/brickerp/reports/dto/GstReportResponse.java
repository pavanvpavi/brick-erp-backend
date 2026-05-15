package com.brickerp.reports.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class GstReportResponse {

    private String period;
    private BigDecimal totalTaxableValue;
    private BigDecimal totalCgst;
    private BigDecimal totalSgst;
    private BigDecimal totalIgst;
    private BigDecimal totalGst;
    private BigDecimal totalInvoiceValue;
    private Long totalInvoices;
    private List<GstInvoiceEntry> invoices;

    @Data
    @Builder
    public static class GstInvoiceEntry {
        private String invoiceNumber;
        private String invoiceDate;
        private String customerName;
        private String customerGstin;
        private BigDecimal taxableValue;
        private BigDecimal taxRate;
        private BigDecimal cgst;
        private BigDecimal sgst;
        private BigDecimal igst;
        private BigDecimal totalGst;
        private BigDecimal invoiceValue;
    }
}