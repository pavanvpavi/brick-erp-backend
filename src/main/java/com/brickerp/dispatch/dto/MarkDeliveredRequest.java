package com.brickerp.dispatch.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class MarkDeliveredRequest {
    private String receivedBy;
    private LocalDate receivedAt;
    private String notes;
}