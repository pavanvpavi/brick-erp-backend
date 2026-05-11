package com.brickerp.manufacturing.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BomItemRequest {

    @NotNull(message = "Material product ID is required")
    private Long materialProductId;

    @NotNull(message = "Quantity required is mandatory")
    @Min(value = 0, message = "Quantity must be positive")
    private Double quantityRequired;

    private String notes;
}