package com.brickerp.product.entity;

import com.brickerp.common.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku", nullable = false, unique = true, length = 50)
    private String sku; // Stock Keeping Unit e.g. BRK-RED-STD-001

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ProductCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uom_id", nullable = false)
    private UnitOfMeasure unitOfMeasure;

    // Specifications
    @Column(name = "length_mm")
    private Double lengthMm;

    @Column(name = "width_mm")
    private Double widthMm;

    @Column(name = "height_mm")
    private Double heightMm;

    @Column(name = "weight_kg")
    private Double weightKg;

    @Column(name = "strength_grade", length = 50)
    private String strengthGrade; // e.g., M5, M7.5, M10

    @Column(name = "material", length = 100)
    private String material; // e.g., Red Clay, Fly Ash, Concrete

    @Column(name = "color", length = 50)
    private String color;

    // Pricing
    @Column(name = "selling_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal sellingPrice;

    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "tax_percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxPercentage = BigDecimal.ZERO;

    // Inventory control
    @Column(name = "minimum_stock_level")
    @Builder.Default
    private Integer minimumStockLevel = 0;

    @Column(name = "reorder_quantity")
    @Builder.Default
    private Integer reorderQuantity = 0;
}