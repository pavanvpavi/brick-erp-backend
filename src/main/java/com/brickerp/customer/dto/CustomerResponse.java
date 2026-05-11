package com.brickerp.customer.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CustomerResponse {
    private Long id;
    private String customerCode;
    private String name;
    private String customerType;
    private String email;
    private String phone;
    private String alternatePhone;
    private String gstin;
    private String pan;
    private Double creditLimit;
    private Integer creditDays;
    private String notes;
    private Boolean isActive;
    private List<CustomerAddressResponse> addresses;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}