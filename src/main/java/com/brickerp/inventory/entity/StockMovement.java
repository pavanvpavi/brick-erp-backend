package com.brickerp.inventory.entity;

import com.brickerp.common.audit.BaseEntity;
import com.brickerp.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stock_movements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 20)
    private MovementType movementType;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "quantity_before", nullable = false)
    private Integer quantityBefore;

    @Column(name = "quantity_after", nullable = false)
    private Integer quantityAfter;

    @Column(name = "reference_type", length = 50)
    private String referenceType; // e.g. SALES_ORDER, PURCHASE_ORDER, MANUAL

    @Column(name = "reference_id")
    private Long referenceId; // ID of the order/document

    @Column(name = "notes", length = 500)
    private String notes;

    public enum MovementType {
        STOCK_IN, // Purchase / Production
        STOCK_OUT, // Sales / Consumption
        ADJUSTMENT_IN, // Manual positive adjustment
        ADJUSTMENT_OUT, // Manual negative adjustment
        TRANSFER_IN, // Transfer from another warehouse
        TRANSFER_OUT // Transfer to another warehouse
    }
}