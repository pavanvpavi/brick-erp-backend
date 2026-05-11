package com.brickerp.finance.controller;

import com.brickerp.common.response.ApiResponse;
import com.brickerp.finance.dto.*;
import com.brickerp.finance.service.FinanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService financeService;

    @PostMapping("/from-order")
    public ResponseEntity<ApiResponse<InvoiceResponse>> createFromOrder(
            @Valid @RequestBody CreateInvoiceFromOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Invoice created successfully",
                        financeService.createInvoiceFromOrder(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(financeService.getInvoiceById(id)));
    }

    @GetMapping("/number/{invoiceNumber}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getByNumber(
            @PathVariable String invoiceNumber) {
        return ResponseEntity.ok(ApiResponse.success(
                financeService.getInvoiceByNumber(invoiceNumber)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InvoiceSummaryResponse>>> getAllInvoices() {
        return ResponseEntity.ok(ApiResponse.success(financeService.getAllInvoices()));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<InvoiceSummaryResponse>>> getByCustomer(
            @PathVariable Long customerId) {
        return ResponseEntity.ok(ApiResponse.success(
                financeService.getInvoicesByCustomer(customerId)));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<InvoiceSummaryResponse>>> getByStatus(
            @PathVariable String status) {
        return ResponseEntity.ok(ApiResponse.success(
                financeService.getInvoicesByStatus(status)));
    }

    @PostMapping("/{id}/send")
    public ResponseEntity<ApiResponse<InvoiceResponse>> sendInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Invoice sent successfully",
                financeService.sendInvoice(id)));
    }

    @PostMapping("/{id}/payments")
    public ResponseEntity<ApiResponse<InvoiceResponse>> recordPayment(
            @PathVariable Long id,
            @Valid @RequestBody RecordPaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Payment recorded successfully",
                financeService.recordPayment(id, request)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<InvoiceResponse>> cancelInvoice(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(ApiResponse.success("Invoice cancelled",
                financeService.cancelInvoice(id, reason)));
    }
}