package com.brickerp.customer.dto;

import lombok.Data;

@Data
public class CustomerSummaryResponse {
    private Long id;
    private String customerCode;
    private String name;
    private String customerType;
    private String phone;
    private String email;
    private String city;
    private Boolean isActive;
}