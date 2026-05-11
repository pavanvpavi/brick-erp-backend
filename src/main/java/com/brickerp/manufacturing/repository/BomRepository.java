package com.brickerp.manufacturing.repository;

import com.brickerp.manufacturing.entity.BillOfMaterials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BomRepository extends JpaRepository<BillOfMaterials, Long> {
    List<BillOfMaterials> findByFinishedProductId(Long productId);

    List<BillOfMaterials> findByIsActiveTrue();

    Optional<BillOfMaterials> findByFinishedProductIdAndIsDefaultTrue(Long productId);
}