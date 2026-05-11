package com.brickerp.order.controller;

import com.brickerp.common.response.ApiResponse;
import com.brickerp.order.dto.*;
import com.brickerp.order.service.SalesOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/sales-orders")
@RequiredArgsConstructor
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    @PostMapping
    public ResponseEntity<ApiResponse<SalesOrderResponse>> createOrder(
            @Valid @RequestBody SalesOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Sales order created successfully",
                        salesOrderService.createOrder(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SalesOrderResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(salesOrderService.getById(id)));
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<ApiResponse<SalesOrderResponse>> getByOrderNumber(
            @PathVariable String orderNumber) {
        return ResponseEntity.ok(ApiResponse.success(
                salesOrderService.getByOrderNumber(orderNumber)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SalesOrderSummaryResponse>>> getAllOrders() {
        return ResponseEntity.ok(ApiResponse.success(salesOrderService.getAllOrders()));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<SalesOrderSummaryResponse>>> getByCustomer(
            @PathVariable Long customerId) {
        return ResponseEntity.ok(ApiResponse.success(
                salesOrderService.getOrdersByCustomer(customerId)));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<SalesOrderSummaryResponse>>> getByStatus(
            @PathVariable String status) {
        return ResponseEntity.ok(ApiResponse.success(
                salesOrderService.getOrdersByStatus(status)));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<SalesOrderSummaryResponse>>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.success(
                salesOrderService.getOrdersByDateRange(startDate, endDate)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<SalesOrderResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Order status updated",
                salesOrderService.updateStatus(id, request)));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<SalesOrderResponse>> confirmOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Order confirmed successfully",
                salesOrderService.confirmOrder(id)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<SalesOrderResponse>> cancelOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully",
                salesOrderService.cancelOrder(id, reason)));
    }
}