package com.brickerp.procurement.controller;

import com.brickerp.common.response.ApiResponse;
import com.brickerp.procurement.dto.*;
import com.brickerp.procurement.service.ProcurementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProcurementController {

    private final ProcurementService procurementService;

    // ==================== SUPPLIER ====================

    @PostMapping("/suppliers")
    public ResponseEntity<ApiResponse<SupplierResponse>> createSupplier(
            @Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Supplier created successfully",
                        procurementService.createSupplier(request)));
    }

    @PutMapping("/suppliers/{id}")
    public ResponseEntity<ApiResponse<SupplierResponse>> updateSupplier(
            @PathVariable Long id, @Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Supplier updated successfully",
                procurementService.updateSupplier(id, request)));
    }

    @GetMapping("/suppliers/{id}")
    public ResponseEntity<ApiResponse<SupplierResponse>> getSupplier(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(procurementService.getSupplierById(id)));
    }

    @GetMapping("/suppliers")
    public ResponseEntity<ApiResponse<List<SupplierResponse>>> getAllSuppliers() {
        return ResponseEntity.ok(ApiResponse.success(procurementService.getAllSuppliers()));
    }

    @DeleteMapping("/suppliers/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSupplier(@PathVariable Long id) {
        procurementService.deleteSupplier(id);
        return ResponseEntity.ok(ApiResponse.success("Supplier deleted successfully", null));
    }

    // ==================== PURCHASE ORDERS ====================

    @PostMapping("/purchase-orders")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> createPurchaseOrder(
            @Valid @RequestBody PurchaseOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Purchase order created successfully",
                        procurementService.createPurchaseOrder(request)));
    }

    @GetMapping("/purchase-orders/{id}")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> getPurchaseOrder(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                procurementService.getPurchaseOrderById(id)));
    }

    @GetMapping("/purchase-orders")
    public ResponseEntity<ApiResponse<List<PurchaseOrderResponse>>> getAllPurchaseOrders() {
        return ResponseEntity.ok(ApiResponse.success(procurementService.getAllPurchaseOrders()));
    }

    @GetMapping("/purchase-orders/supplier/{supplierId}")
    public ResponseEntity<ApiResponse<List<PurchaseOrderResponse>>> getBySupplier(
            @PathVariable Long supplierId) {
        return ResponseEntity.ok(ApiResponse.success(
                procurementService.getPurchaseOrdersBySupplier(supplierId)));
    }

    @GetMapping("/purchase-orders/status/{status}")
    public ResponseEntity<ApiResponse<List<PurchaseOrderResponse>>> getByStatus(
            @PathVariable String status) {
        return ResponseEntity.ok(ApiResponse.success(
                procurementService.getPurchaseOrdersByStatus(status)));
    }

    @PostMapping("/purchase-orders/{id}/send")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> sendToSupplier(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Purchase order sent to supplier",
                procurementService.sendToSupplier(id)));
    }

    @PostMapping("/purchase-orders/{id}/receive")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> receiveItems(
            @PathVariable Long id,
            @Valid @RequestBody ReceiveItemsRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Items received successfully",
                procurementService.receiveItems(id, request)));
    }

    @PostMapping("/purchase-orders/{id}/cancel")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> cancelPurchaseOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(ApiResponse.success("Purchase order cancelled",
                procurementService.cancelPurchaseOrder(id, reason)));
    }
}