package com.brickerp.dispatch.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class DeliveryOrderRequest {

    @NotNull(message = "Sales order ID is required")
    private Long salesOrderId;

    private LocalDate deliveryDate;
    private String vehicleNumber;
    private String driverName;
    private String driverPhone;
    private String deliveryAddress;
    private String notes;
}