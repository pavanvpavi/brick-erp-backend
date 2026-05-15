package com.brickerp.customer.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CustomerLedgerResponse {
    private Long customerId;
    private String customerName;
    private String customerCode;
    private BigDecimal totalInvoiced;
    private BigDecimal totalPaid;
    private BigDecimal totalOutstanding;
    private List<LedgerEntryResponse> entries;
}