package com.brickerp.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CustomerRequest {

    @NotBlank(message = "Customer name is required")
    @Size(max = 150)
    private String name;

    @NotNull(message = "Customer type is required")
    private String customerType;

    private String email;
    private String phone;
    private String alternatePhone;
    private String gstin;
    private String pan;
    private Double creditLimit;
    private Integer creditDays;
    private String notes;
    private List<CustomerAddressRequest> addresses;
}