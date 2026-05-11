package com.brickerp.procurement.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SupplierResponse {
    private Long id;
    private String supplierCode;
    private String name;
    private String contactPerson;
    private String email;
    private String phone;
    private String gstin;
    private String pan;
    private String addressLine1;
    private String city;
    private String state;
    private String pincode;
    private Integer paymentTermsDays;
    private String notes;
    private Boolean isActive;
    private LocalDateTime createdAt;
}