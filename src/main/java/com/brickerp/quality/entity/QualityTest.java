package com.brickerp.quality.entity;

import com.brickerp.common.audit.BaseEntity;
import com.brickerp.manufacturing.entity.ProductionOrder;
import com.brickerp.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "quality_tests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QualityTest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "test_number", nullable = false, unique = true, length = 20)
    private String testNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_order_id")
    private ProductionOrder productionOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "test_date", nullable = false)
    private LocalDate testDate;

    @Column(name = "batch_size", nullable = false)
    private Integer batchSize;

    @Column(name = "passed_quantity", nullable = false)
    private Integer passedQuantity;

    @Column(name = "rejected_quantity", nullable = false)
    private Integer rejectedQuantity;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false, length = 10)
    private TestResult result;

    // Brick specific tests
    @Column(name = "compressive_strength")
    private Double compressiveStrength; // N/mm²

    @Column(name = "water_absorption_percentage")
    private Double waterAbsorptionPercentage;

    @Column(name = "efflorescence", length = 50)
    private String efflorescence; // NIL, SLIGHT, MODERATE, HEAVY

    @Column(name = "tested_by", length = 100)
    private String testedBy;

    @Column(name = "notes", length = 500)
    private String notes;

    public enum TestResult {
        PASS, FAIL, PARTIAL
    }
}