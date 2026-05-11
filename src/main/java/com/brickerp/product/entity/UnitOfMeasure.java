package com.brickerp.product.entity;

import com.brickerp.common.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "units_of_measure")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnitOfMeasure extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name; // e.g., "Piece", "Dozen", "Pallet"

    @Column(name = "abbreviation", nullable = false, unique = true, length = 10)
    private String abbreviation; // e.g., "PCS", "DOZ", "PLT"

    @Column(name = "description", length = 255)
    private String description;
}