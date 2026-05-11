package com.brickerp.manufacturing.repository;

import com.brickerp.manufacturing.entity.ProductionConsumption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductionConsumptionRepository extends JpaRepository<ProductionConsumption, Long> {
    List<ProductionConsumption> findByProductionOrderId(Long productionOrderId);
}