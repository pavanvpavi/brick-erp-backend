package com.brickerp.order.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SalesOrderSummaryResponse {
    private Long id;
    private String orderNumber;
    private String customerName;
    private String customerCode;
    private String status;
    private LocalDate orderDate;
    private BigDecimal totalAmount;
    private Integer itemCount;
    private LocalDateTime createdAt;
}