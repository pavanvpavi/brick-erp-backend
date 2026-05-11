package com.brickerp.procurement.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class PurchaseOrderRequest {

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;

    private LocalDate expectedDeliveryDate;

    @NotNull(message = "Items are required")
    @Size(min = 1, message = "At least one item is required")
    private List<PurchaseOrderItemRequest> items;

    private String notes;
}