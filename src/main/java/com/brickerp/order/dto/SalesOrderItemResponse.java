package com.brickerp.order.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SalesOrderItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private String uomAbbreviation;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal taxPercentage;
    private BigDecimal taxAmount;
    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;
    private BigDecimal lineTotal;
    private String notes;
}