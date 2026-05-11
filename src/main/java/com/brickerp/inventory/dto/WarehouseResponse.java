package com.brickerp.inventory.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WarehouseResponse {
    private Long id;
    private String name;
    private String code;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String contactPerson;
    private String contactPhone;
    private Boolean isDefault;
    private Boolean isActive;
    private LocalDateTime createdAt;
}