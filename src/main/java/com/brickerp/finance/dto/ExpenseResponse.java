package com.brickerp.finance.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ExpenseResponse {
    private Long id;
    private String expenseNumber;
    private String category;
    private String description;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private String paidTo;
    private String paymentMethod;
    private String referenceNumber;
    private String notes;
    private LocalDateTime createdAt;
}