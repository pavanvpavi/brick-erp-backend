package com.brickerp.manufacturing.controller;

import com.brickerp.common.response.ApiResponse;
import com.brickerp.manufacturing.dto.*;
import com.brickerp.manufacturing.service.ManufacturingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ManufacturingController {

    private final ManufacturingService manufacturingService;

    // ==================== BOM ====================

    @PostMapping("/boms")
    public ResponseEntity<ApiResponse<BomResponse>> createBom(
            @Valid @RequestBody BomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("BOM created successfully",
                        manufacturingService.createBom(request)));
    }

    @GetMapping("/boms/{id}")
    public ResponseEntity<ApiResponse<BomResponse>> getBom(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(manufacturingService.getBomById(id)));
    }

    @GetMapping("/boms")
    public ResponseEntity<ApiResponse<List<BomResponse>>> getAllBoms() {
        return ResponseEntity.ok(ApiResponse.success(manufacturingService.getAllBoms()));
    }

    @GetMapping("/boms/product/{productId}")
    public ResponseEntity<ApiResponse<List<BomResponse>>> getBomsByProduct(
            @PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(
                manufacturingService.getBomsByProduct(productId)));
    }

    @DeleteMapping("/boms/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBom(@PathVariable Long id) {
        manufacturingService.deleteBom(id);
        return ResponseEntity.ok(ApiResponse.success("BOM deleted successfully", null));
    }

    // ==================== PRODUCTION ORDERS ====================

    @PostMapping("/production-orders")
    public ResponseEntity<ApiResponse<ProductionOrderResponse>> createProductionOrder(
            @Valid @RequestBody ProductionOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Production order created successfully",
                        manufacturingService.createProductionOrder(request)));
    }

    @GetMapping("/production-orders/{id}")
    public ResponseEntity<ApiResponse<ProductionOrderResponse>> getProductionOrder(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                manufacturingService.getProductionOrderById(id)));
    }

    @GetMapping("/production-orders")
    public ResponseEntity<ApiResponse<List<ProductionOrderResponse>>> getAllProductionOrders() {
        return ResponseEntity.ok(ApiResponse.success(
                manufacturingService.getAllProductionOrders()));
    }

    @GetMapping("/production-orders/status/{status}")
    public ResponseEntity<ApiResponse<List<ProductionOrderResponse>>> getByStatus(
            @PathVariable String status) {
        return ResponseEntity.ok(ApiResponse.success(
                manufacturingService.getProductionOrdersByStatus(status)));
    }

    @PostMapping("/production-orders/{id}/start")
    public ResponseEntity<ApiResponse<ProductionOrderResponse>> startProduction(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Production started",
                manufacturingService.startProduction(id)));
    }

    @PostMapping("/production-orders/{id}/complete")
    public ResponseEntity<ApiResponse<ProductionOrderResponse>> completeProduction(
            @PathVariable Long id,
            @Valid @RequestBody CompleteProductionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Production completed successfully",
                manufacturingService.completeProduction(id, request)));
    }

    @PostMapping("/production-orders/{id}/cancel")
    public ResponseEntity<ApiResponse<ProductionOrderResponse>> cancelProductionOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(ApiResponse.success("Production order cancelled",
                manufacturingService.cancelProductionOrder(id, reason)));
    }
}