package com.brickerp.product.controller;

import com.brickerp.common.response.ApiResponse;
import com.brickerp.product.dto.UomRequest;
import com.brickerp.product.dto.UomResponse;
import com.brickerp.product.service.UomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/uom")
@RequiredArgsConstructor
public class UomController {

    private final UomService uomService;

    @PostMapping
    public ResponseEntity<ApiResponse<UomResponse>> create(@Valid @RequestBody UomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("UOM created successfully", uomService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UomResponse>> update(
            @PathVariable Long id, @Valid @RequestBody UomRequest request) {
        return ResponseEntity.ok(ApiResponse.success("UOM updated successfully",
                uomService.update(id, request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UomResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(uomService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UomResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(uomService.getAllActive()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        uomService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("UOM deleted successfully", null));
    }
}