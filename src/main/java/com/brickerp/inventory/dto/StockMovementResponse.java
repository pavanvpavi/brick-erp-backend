package com.brickerp.inventory.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StockMovementResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private Long warehouseId;
    private String warehouseName;
    private String movementType;
    private Integer quantity;
    private Integer quantityBefore;
    private Integer quantityAfter;
    private String referenceType;
    private Long referenceId;
    private String notes;
    private LocalDateTime createdAt;
}