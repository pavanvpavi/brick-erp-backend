package com.brickerp.transfer.controller;

import com.brickerp.common.response.ApiResponse;
import com.brickerp.transfer.dto.StockTransferRequest;
import com.brickerp.transfer.dto.StockTransferResponse;
import com.brickerp.transfer.service.StockTransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stock-transfers")
@RequiredArgsConstructor
public class StockTransferController {

    private final StockTransferService transferService;

    @PostMapping
    public ResponseEntity<ApiResponse<StockTransferResponse>> transfer(
            @Valid @RequestBody StockTransferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Stock transferred successfully",
                        transferService.transfer(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StockTransferResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(transferService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StockTransferResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(transferService.getById(id)));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<StockTransferResponse>>> getByProduct(
            @PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(transferService.getByProduct(productId)));
    }

    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<ApiResponse<List<StockTransferResponse>>> getByWarehouse(
            @PathVariable Long warehouseId) {
        return ResponseEntity.ok(ApiResponse.success(transferService.getByWarehouse(warehouseId)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<StockTransferResponse>> cancel(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(ApiResponse.success("Transfer cancelled",
                transferService.cancelTransfer(id, reason)));
    }
}