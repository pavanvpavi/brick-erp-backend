package com.brickerp.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WarehouseRequest {

    @NotBlank(message = "Warehouse name is required")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Warehouse code is required")
    @Size(max = 20)
    private String code;

    @Size(max = 500)
    private String address;

    private String city;
    private String state;
    private String pincode;
    private String contactPerson;
    private String contactPhone;
    private Boolean isDefault = false;
}