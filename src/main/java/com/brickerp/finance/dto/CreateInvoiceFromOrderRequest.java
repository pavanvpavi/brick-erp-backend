package com.brickerp.finance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateInvoiceFromOrderRequest {

    @NotNull(message = "Sales order ID is required")
    private Long salesOrderId;

    private LocalDate dueDate;
    private String notes;
}