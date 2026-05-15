package com.brickerp.customer.controller;

import com.brickerp.common.response.ApiResponse;
import com.brickerp.customer.dto.*;
import com.brickerp.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> create(
            @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer created successfully",
                        customerService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Customer updated successfully",
                customerService.update(id, request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getById(id)));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getByCode(code)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerSummaryResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(customerService.getAll()));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CustomerSummaryResponse>>> search(
            @RequestParam String keyword) {
        return ResponseEntity.ok(ApiResponse.success(customerService.search(keyword)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Customer deleted successfully", null));
    }

    // ==================== ADDRESS ====================

    @PostMapping("/{customerId}/addresses")
    public ResponseEntity<ApiResponse<CustomerResponse>> addAddress(
            @PathVariable Long customerId,
            @Valid @RequestBody CustomerAddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Address added successfully",
                        customerService.addAddress(customerId, request)));
    }

    @PutMapping("/{customerId}/addresses/{addressId}")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateAddress(
            @PathVariable Long customerId,
            @PathVariable Long addressId,
            @Valid @RequestBody CustomerAddressRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Address updated successfully",
                customerService.updateAddress(customerId, addressId, request)));
    }

    @DeleteMapping("/{customerId}/addresses/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @PathVariable Long customerId,
            @PathVariable Long addressId) {
        customerService.deleteAddress(customerId, addressId);
        return ResponseEntity.ok(ApiResponse.success("Address deleted successfully", null));
    }

    @GetMapping("/{id}/ledger")
    public ResponseEntity<ApiResponse<CustomerLedgerResponse>> getLedger(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getCustomerLedger(id)));
    }
}