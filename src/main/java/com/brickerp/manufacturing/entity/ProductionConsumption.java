package com.brickerp.manufacturing.entity;

import com.brickerp.common.audit.BaseEntity;
import com.brickerp.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "production_consumptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionConsumption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_order_id", nullable = false)
    private ProductionOrder productionOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_product_id", nullable = false)
    private Product materialProduct;

    @Column(name = "planned_quantity", nullable = false)
    private Double plannedQuantity;

    @Column(name = "consumed_quantity", nullable = false)
    @Builder.Default
    private Double consumedQuantity = 0.0;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;
}