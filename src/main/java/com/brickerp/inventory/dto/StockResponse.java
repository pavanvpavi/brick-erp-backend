package com.brickerp.inventory.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StockResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private Long warehouseId;
    private String warehouseName;
    private String warehouseCode;
    private Integer quantityOnHand;
    private Integer quantityReserved;
    private Integer availableQuantity;
    private Boolean isLowStock;
    private Integer minimumStockLevel;
    private LocalDateTime updatedAt;
}