package com.brickerp.inventory.controller;

import com.brickerp.common.response.ApiResponse;
import com.brickerp.inventory.dto.*;
import com.brickerp.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // ==================== WAREHOUSE ====================

    @PostMapping("/warehouses")
    public ResponseEntity<ApiResponse<WarehouseResponse>> createWarehouse(
            @Valid @RequestBody WarehouseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Warehouse created successfully",
                        inventoryService.createWarehouse(request)));
    }

    @PutMapping("/warehouses/{id}")
    public ResponseEntity<ApiResponse<WarehouseResponse>> updateWarehouse(
            @PathVariable Long id, @Valid @RequestBody WarehouseRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Warehouse updated successfully",
                inventoryService.updateWarehouse(id, request)));
    }

    @GetMapping("/warehouses/{id}")
    public ResponseEntity<ApiResponse<WarehouseResponse>> getWarehouse(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getWarehouseById(id)));
    }

    @GetMapping("/warehouses")
    public ResponseEntity<ApiResponse<List<WarehouseResponse>>> getAllWarehouses() {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getAllWarehouses()));
    }

    @DeleteMapping("/warehouses/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWarehouse(@PathVariable Long id) {
        inventoryService.deleteWarehouse(id);
        return ResponseEntity.ok(ApiResponse.success("Warehouse deleted successfully", null));
    }

    // ==================== STOCK ====================

    @GetMapping("/stock")
    public ResponseEntity<ApiResponse<StockResponse>> getStock(
            @RequestParam Long productId,
            @RequestParam Long warehouseId) {
        return ResponseEntity.ok(ApiResponse.success(
                inventoryService.getStock(productId, warehouseId)));
    }

    @GetMapping("/stock/warehouse/{warehouseId}")
    public ResponseEntity<ApiResponse<List<StockResponse>>> getStockByWarehouse(
            @PathVariable Long warehouseId) {
        return ResponseEntity.ok(ApiResponse.success(
                inventoryService.getStockByWarehouse(warehouseId)));
    }

    @GetMapping("/stock/product/{productId}")
    public ResponseEntity<ApiResponse<List<StockResponse>>> getStockByProduct(
            @PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(
                inventoryService.getStockByProduct(productId)));
    }

    @GetMapping("/stock/low-stock")
    public ResponseEntity<ApiResponse<List<StockResponse>>> getLowStockItems() {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getLowStockItems()));
    }

    @GetMapping("/stock/total/{productId}")
    public ResponseEntity<ApiResponse<Integer>> getTotalStock(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getTotalStock(productId)));
    }

    // ==================== STOCK ADJUSTMENT ====================

    @PostMapping("/stock/adjust")
    public ResponseEntity<ApiResponse<StockMovementResponse>> adjustStock(
            @Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Stock adjusted successfully",
                inventoryService.adjustStock(request)));
    }

    // ==================== MOVEMENTS ====================

    @GetMapping("/stock/movements/product/{productId}")
    public ResponseEntity<ApiResponse<List<StockMovementResponse>>> getMovementsByProduct(
            @PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(
                inventoryService.getMovementsByProduct(productId)));
    }

    @GetMapping("/stock/movements/warehouse/{warehouseId}")
    public ResponseEntity<ApiResponse<List<StockMovementResponse>>> getMovementsByWarehouse(
            @PathVariable Long warehouseId) {
        return ResponseEntity.ok(ApiResponse.success(
                inventoryService.getMovementsByWarehouse(warehouseId)));
    }
}