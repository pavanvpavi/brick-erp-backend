package com.brickerp.manufacturing.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BomRequest {

    @NotNull(message = "Finished product ID is required")
    private Long finishedProductId;

    @NotBlank(message = "BOM name is required")
    private String name;

    private String version;

    @NotNull(message = "Output quantity is required")
    @Min(value = 1, message = "Output quantity must be at least 1")
    private Integer outputQuantity;

    private String description;
    private Boolean isDefault = false;

    @NotNull(message = "BOM items are required")
    private List<BomItemRequest> bomItems;
}