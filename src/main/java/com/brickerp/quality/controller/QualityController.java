package com.brickerp.quality.controller;

import com.brickerp.common.response.ApiResponse;
import com.brickerp.quality.dto.*;
import com.brickerp.quality.service.QualityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quality-tests")
@RequiredArgsConstructor
public class QualityController {

    private final QualityService qualityService;

    @PostMapping
    public ResponseEntity<ApiResponse<QualityTestResponse>> create(
            @Valid @RequestBody QualityTestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Quality test recorded",
                        qualityService.createTest(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<QualityTestResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(qualityService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QualityTestResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(qualityService.getById(id)));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<QualityTestResponse>>> getByProduct(
            @PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(qualityService.getByProduct(productId)));
    }

    @GetMapping("/production-order/{productionOrderId}")
    public ResponseEntity<ApiResponse<List<QualityTestResponse>>> getByProductionOrder(
            @PathVariable Long productionOrderId) {
        return ResponseEntity.ok(ApiResponse.success(
                qualityService.getByProductionOrder(productionOrderId)));
    }
}