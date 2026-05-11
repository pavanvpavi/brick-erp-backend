package com.brickerp.inventory.repository;

import com.brickerp.inventory.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    List<Stock> findByWarehouseId(Long warehouseId);

    List<Stock> findByProductId(Long productId);

    // Find all stocks where quantity is below product's minimum stock level
    @Query("SELECT s FROM Stock s WHERE s.quantityOnHand <= s.product.minimumStockLevel AND s.isActive = true")
    List<Stock> findLowStockItems();

    // Total stock across all warehouses for a product
    @Query("SELECT COALESCE(SUM(s.quantityOnHand), 0) FROM Stock s WHERE s.product.id = :productId")
    Integer getTotalStockByProduct(Long productId);
}