package com.brickerp.quality.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class QualityTestResponse {
    private Long id;
    private String testNumber;
    private Long productId;
    private String productName;
    private Long productionOrderId;
    private String productionNumber;
    private LocalDate testDate;
    private Integer batchSize;
    private Integer passedQuantity;
    private Integer rejectedQuantity;
    private Double passRate;
    private String rejectionReason;
    private String result;
    private Double compressiveStrength;
    private Double waterAbsorptionPercentage;
    private String efflorescence;
    private String testedBy;
    private String notes;
    private LocalDateTime createdAt;
}