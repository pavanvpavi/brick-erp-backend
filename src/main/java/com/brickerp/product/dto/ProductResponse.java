package com.brickerp.product.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductResponse {
    private Long id;
    private String sku;
    private String name;
    private String description;

    private Long categoryId;
    private String categoryName;

    private Long uomId;
    private String uomName;
    private String uomAbbreviation;

    // Specifications
    private Double lengthMm;
    private Double widthMm;
    private Double heightMm;
    private Double weightKg;
    private String strengthGrade;
    private String material;
    private String color;

    // Pricing
    private BigDecimal sellingPrice;
    private BigDecimal costPrice;
    private BigDecimal taxPercentage;

    // Inventory
    private Integer minimumStockLevel;
    private Integer reorderQuantity;

    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}