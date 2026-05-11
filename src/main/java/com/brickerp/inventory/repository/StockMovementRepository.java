package com.brickerp.inventory.repository;

import com.brickerp.inventory.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findByProductIdOrderByCreatedAtDesc(Long productId);

    List<StockMovement> findByWarehouseIdOrderByCreatedAtDesc(Long warehouseId);

    List<StockMovement> findByProductIdAndWarehouseIdOrderByCreatedAtDesc(
            Long productId, Long warehouseId);

    List<StockMovement> findByReferenceTypeAndReferenceId(
            String referenceType, Long referenceId);
}