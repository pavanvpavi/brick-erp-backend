package com.brickerp.dispatch.controller;

import com.brickerp.common.response.ApiResponse;
import com.brickerp.dispatch.dto.*;
import com.brickerp.dispatch.service.DispatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
public class DispatchController {

    private final DispatchService dispatchService;

    @PostMapping
    public ResponseEntity<ApiResponse<DeliveryOrderResponse>> create(
            @Valid @RequestBody DeliveryOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Delivery order created",
                        dispatchService.createDeliveryOrder(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DeliveryOrderResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(dispatchService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeliveryOrderResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(dispatchService.getById(id)));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<DeliveryOrderResponse>>> getByStatus(
            @PathVariable String status) {
        return ResponseEntity.ok(ApiResponse.success(dispatchService.getByStatus(status)));
    }

    @PostMapping("/{id}/dispatch")
    public ResponseEntity<ApiResponse<DeliveryOrderResponse>> dispatch(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Delivery dispatched",
                dispatchService.dispatch(id)));
    }

    @PostMapping("/{id}/deliver")
    public ResponseEntity<ApiResponse<DeliveryOrderResponse>> markDelivered(
            @PathVariable Long id,
            @RequestBody MarkDeliveredRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Marked as delivered",
                dispatchService.markDelivered(id, request)));
    }

    @PostMapping("/{id}/fail")
    public ResponseEntity<ApiResponse<DeliveryOrderResponse>> markFailed(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(ApiResponse.success("Marked as failed",
                dispatchService.markFailed(id, reason)));
    }
}