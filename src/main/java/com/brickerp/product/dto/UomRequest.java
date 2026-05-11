package com.brickerp.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UomRequest {

    @NotBlank(message = "UOM name is required")
    @Size(max = 50)
    private String name;

    @NotBlank(message = "Abbreviation is required")
    @Size(max = 10)
    private String abbreviation;

    private String description;
}