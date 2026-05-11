package com.brickerp.manufacturing.dto;

import lombok.Data;

@Data
public class ProductionConsumptionResponse {
    private Long id;
    private Long materialProductId;
    private String materialProductName;
    private String materialProductSku;
    private Double plannedQuantity;
    private Double consumedQuantity;
    private Long warehouseId;
}