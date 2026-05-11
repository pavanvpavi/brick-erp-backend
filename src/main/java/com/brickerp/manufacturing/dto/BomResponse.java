package com.brickerp.manufacturing.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BomResponse {
    private Long id;
    private Long finishedProductId;
    private String finishedProductName;
    private String finishedProductSku;
    private String name;
    private String version;
    private Integer outputQuantity;
    private String description;
    private Boolean isDefault;
    private Boolean isActive;
    private List<BomItemResponse> bomItems;
    private LocalDateTime createdAt;
}