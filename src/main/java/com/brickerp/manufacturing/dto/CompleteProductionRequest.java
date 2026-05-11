package com.brickerp.manufacturing.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CompleteProductionRequest {

    @NotNull(message = "Produced quantity is required")
    @Min(value = 1, message = "Produced quantity must be at least 1")
    private Integer producedQuantity;

    private String notes;
}