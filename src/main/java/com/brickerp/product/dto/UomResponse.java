package com.brickerp.product.dto;

import lombok.Data;

@Data
public class UomResponse {
    private Long id;
    private String name;
    private String abbreviation;
    private String description;
    private Boolean isActive;
}