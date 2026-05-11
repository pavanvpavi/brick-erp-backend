package com.brickerp.manufacturing.dto;

import lombok.Data;

@Data
public class BomItemResponse {
    private Long id;
    private Long materialProductId;
    private String materialProductName;
    private String materialProductSku;
    private String uomAbbreviation;
    private Double quantityRequired;
    private String notes;
}