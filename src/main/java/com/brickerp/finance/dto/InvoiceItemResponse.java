package com.brickerp.finance.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class InvoiceItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private String description;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal taxPercentage;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal lineTotal;
}