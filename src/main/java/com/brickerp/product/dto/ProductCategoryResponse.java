package com.brickerp.product.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProductCategoryResponse {
    private Long id;
    private String name;
    private String description;
    private String code;
    private Boolean isActive;
    private LocalDateTime createdAt;
}