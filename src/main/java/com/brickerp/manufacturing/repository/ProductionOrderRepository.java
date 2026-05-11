package com.brickerp.manufacturing.repository;

import com.brickerp.manufacturing.entity.ProductionOrder;
import com.brickerp.manufacturing.entity.ProductionOrder.ProductionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductionOrderRepository extends JpaRepository<ProductionOrder, Long> {
    List<ProductionOrder> findAllByOrderByCreatedAtDesc();

    List<ProductionOrder> findByStatusOrderByCreatedAtDesc(ProductionStatus status);

    List<ProductionOrder> findByFinishedProductIdOrderByCreatedAtDesc(Long productId);

    @Query("SELECT MAX(p.productionNumber) FROM ProductionOrder p WHERE p.productionNumber LIKE 'PRD-%'")
    Optional<String> findLastProductionNumber();
}