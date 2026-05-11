package com.brickerp.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class SalesOrderRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    private Long deliveryAddressId;

    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;

    private LocalDate expectedDeliveryDate;

    @Size(min = 1, message = "At least one item is required")
    @NotNull(message = "Order items are required")
    private List<SalesOrderItemRequest> items;

    private String notes;
}