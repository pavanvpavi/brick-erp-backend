package com.brickerp.customer.dto;

import lombok.Data;

@Data
public class CustomerAddressResponse {
    private Long id;
    private String addressType;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String pincode;
    private String country;
    private Boolean isDefault;
    private String contactName;
    private String contactPhone;
}