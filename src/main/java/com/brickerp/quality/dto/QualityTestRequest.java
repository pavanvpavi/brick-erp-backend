package com.brickerp.quality.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;
import java.time.LocalDate;

@Data
public class QualityTestRequest {

    @NotNull
    private Long productId;

    private Long productionOrderId;

    @NotNull
    private LocalDate testDate;

    @NotNull
    @Min(1)
    private Integer batchSize;

    @NotNull
    @Min(0)
    private Integer passedQuantity;

    @NotNull
    @Min(0)
    private Integer rejectedQuantity;

    private String rejectionReason;
    private Double compressiveStrength;
    private Double waterAbsorptionPercentage;
    private String efflorescence;
    private String testedBy;
    private String notes;
}