package com.brickerp.procurement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class ReceiveItemsRequest {

    @NotNull
    private List<ReceivedItem> items;

    private String notes;

    @Data
    public static class ReceivedItem {
        @NotNull(message = "Item ID is required")
        private Long itemId;

        @NotNull(message = "Received quantity is required")
        @Min(value = 1, message = "Received quantity must be at least 1")
        private Integer receivedQuantity;
    }
}