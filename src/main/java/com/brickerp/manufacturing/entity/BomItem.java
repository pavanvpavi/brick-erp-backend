package com.brickerp.manufacturing.entity;

import com.brickerp.common.audit.BaseEntity;
import com.brickerp.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bom_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BomItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bom_id", nullable = false)
    private BillOfMaterials billOfMaterials;

    // Raw material / component product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_product_id", nullable = false)
    private Product materialProduct;

    // Quantity needed per BOM output run
    @Column(name = "quantity_required", nullable = false)
    private Double quantityRequired;

    @Column(name = "notes", length = 255)
    private String notes;
}