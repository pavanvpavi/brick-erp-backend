package com.brickerp.procurement.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PurchaseOrderItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private Integer quantityOrdered;
    private Integer quantityReceived;
    private Integer pendingQuantity;
    private BigDecimal unitPrice;
    private BigDecimal taxPercentage;
    private BigDecimal taxAmount;
    private BigDecimal lineTotal;
    private String notes;
}