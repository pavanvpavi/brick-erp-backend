package com.brickerp.inventory.service;

import com.brickerp.inventory.dto.*;
import com.brickerp.inventory.entity.StockMovement;

import java.util.List;

public interface InventoryService {

    // Warehouse
    WarehouseResponse createWarehouse(WarehouseRequest request);

    WarehouseResponse updateWarehouse(Long id, WarehouseRequest request);

    WarehouseResponse getWarehouseById(Long id);

    List<WarehouseResponse> getAllWarehouses();

    void deleteWarehouse(Long id);

    // Stock
    StockResponse getStock(Long productId, Long warehouseId);

    List<StockResponse> getStockByWarehouse(Long warehouseId);

    List<StockResponse> getStockByProduct(Long productId);

    List<StockResponse> getLowStockItems();

    Integer getTotalStock(Long productId);

    // Movements
    StockMovementResponse adjustStock(StockAdjustmentRequest request);

    void addStock(Long productId, Long warehouseId, Integer quantity,
            String referenceType, Long referenceId, String notes);

    void deductStock(Long productId, Long warehouseId, Integer quantity,
            String referenceType, Long referenceId, String notes);

    List<StockMovementResponse> getMovementsByProduct(Long productId);

    List<StockMovementResponse> getMovementsByWarehouse(Long warehouseId);
}