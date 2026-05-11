package com.brickerp.manufacturing.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductionOrderResponse {
    private Long id;
    private String productionNumber;
    private Long finishedProductId;
    private String finishedProductName;
    private String finishedProductSku;
    private Long bomId;
    private String bomName;
    private String status;
    private Integer plannedQuantity;
    private Integer producedQuantity;
    private Long warehouseId;
    private String warehouseName;
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private LocalDate actualStartDate;
    private LocalDate actualEndDate;
    private String notes;
    private List<ProductionConsumptionResponse> consumptions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}