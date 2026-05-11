package com.brickerp.manufacturing.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ProductionOrderRequest {

    @NotNull(message = "BOM ID is required")
    private Long bomId;

    @NotNull(message = "Planned quantity is required")
    @Min(value = 1, message = "Planned quantity must be at least 1")
    private Integer plannedQuantity;

    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;

    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private String notes;
}