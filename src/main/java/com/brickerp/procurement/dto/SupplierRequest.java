package com.brickerp.procurement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SupplierRequest {

    @NotBlank(message = "Supplier name is required")
    @Size(max = 150)
    private String name;

    private String contactPerson;
    private String email;
    private String phone;
    private String alternatePhone;
    private String gstin;
    private String pan;
    private String addressLine1;
    private String city;
    private String state;
    private String pincode;
    private Integer paymentTermsDays;
    private String notes;
}