package com.brickerp.manufacturing.entity;

import com.brickerp.common.audit.BaseEntity;
import com.brickerp.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bill_of_materials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillOfMaterials extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The finished product this BOM produces
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finished_product_id", nullable = false)
    private Product finishedProduct;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "version", length = 10)
    private String version;

    // How many units this BOM produces in one run
    @Column(name = "output_quantity", nullable = false)
    private Integer outputQuantity;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    @OneToMany(mappedBy = "billOfMaterials", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BomItem> bomItems = new ArrayList<>();
}