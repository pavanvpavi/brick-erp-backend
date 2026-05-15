package com.brickerp.transfer.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StockTransferResponse {
    private Long id;
    private String transferNumber;
    private Long productId;
    private String productName;
    private String productSku;
    private Long fromWarehouseId;
    private String fromWarehouseName;
    private Long toWarehouseId;
    private String toWarehouseName;
    private Integer quantity;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
}