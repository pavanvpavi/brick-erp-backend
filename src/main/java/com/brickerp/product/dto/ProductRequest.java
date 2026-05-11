package com.brickerp.product.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    @NotBlank(message = "SKU is required")
    @Size(max = 50)
    private String sku;

    @NotBlank(message = "Product name is required")
    @Size(max = 150)
    private String name;

    @Size(max = 1000)
    private String description;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Unit of Measure ID is required")
    private Long uomId;

    // Specifications
    private Double lengthMm;
    private Double widthMm;
    private Double heightMm;
    private Double weightKg;
    private String strengthGrade;
    private String material;
    private String color;

    // Pricing
    @NotNull(message = "Selling price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Selling price must be positive")
    private BigDecimal sellingPrice;

    private BigDecimal costPrice;

    @DecimalMin(value = "0.0", message = "Tax percentage cannot be negative")
    @DecimalMax(value = "100.0", message = "Tax percentage cannot exceed 100")
    private BigDecimal taxPercentage;

    @Min(value = 0, message = "Minimum stock level cannot be negative")
    private Integer minimumStockLevel;

    @Min(value = 0, message = "Reorder quantity cannot be negative")
    private Integer reorderQuantity;
}