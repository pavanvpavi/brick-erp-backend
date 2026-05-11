package com.brickerp.dispatch.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DeliveryOrderResponse {
    private Long id;
    private String deliveryNumber;
    private Long salesOrderId;
    private String salesOrderNumber;
    private String customerName;
    private String status;
    private LocalDate deliveryDate;
    private String vehicleNumber;
    private String driverName;
    private String driverPhone;
    private String deliveryAddress;
    private String notes;
    private String receivedBy;
    private LocalDate receivedAt;
    private LocalDateTime createdAt;
}