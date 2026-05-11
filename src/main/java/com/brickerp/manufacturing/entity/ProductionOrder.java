package com.brickerp.manufacturing.entity;

import com.brickerp.common.audit.BaseEntity;
import com.brickerp.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "production_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionOrder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "production_number", nullable = false, unique = true, length = 20)
    private String productionNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finished_product_id", nullable = false)
    private Product finishedProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bom_id", nullable = false)
    private BillOfMaterials billOfMaterials;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ProductionStatus status = ProductionStatus.PLANNED;

    @Column(name = "planned_quantity", nullable = false)
    private Integer plannedQuantity;

    @Column(name = "produced_quantity", nullable = false)
    @Builder.Default
    private Integer producedQuantity = 0;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "planned_start_date")
    private LocalDate plannedStartDate;

    @Column(name = "planned_end_date")
    private LocalDate plannedEndDate;

    @Column(name = "actual_start_date")
    private LocalDate actualStartDate;

    @Column(name = "actual_end_date")
    private LocalDate actualEndDate;

    @Column(name = "notes", length = 1000)
    private String notes;

    @OneToMany(mappedBy = "productionOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductionConsumption> consumptions = new ArrayList<>();

    public enum ProductionStatus {
        PLANNED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }
}