package com.brickerp.reports.controller;

import com.brickerp.common.response.ApiResponse;
import com.brickerp.reports.dto.*;
import com.brickerp.reports.service.ReportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportsController {

    private final ReportsService reportsService;

    @GetMapping("/gst")
    public ResponseEntity<ApiResponse<GstReportResponse>> getGstReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate == null)
            startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null)
            endDate = LocalDate.now();
        return ResponseEntity.ok(ApiResponse.success(
                reportsService.getGstReport(startDate, endDate)));
    }

    @GetMapping("/sales")
    public ResponseEntity<ApiResponse<SalesReportDetailResponse>> getSalesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate == null)
            startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null)
            endDate = LocalDate.now();
        return ResponseEntity.ok(ApiResponse.success(
                reportsService.getSalesReport(startDate, endDate)));
    }

    @GetMapping("/sales/customer/{customerId}")
    public ResponseEntity<ApiResponse<CustomerSalesReportResponse>> getCustomerSalesReport(
            @PathVariable Long customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate == null)
            startDate = LocalDate.now().minusMonths(3);
        if (endDate == null)
            endDate = LocalDate.now();
        return ResponseEntity.ok(ApiResponse.success(
                reportsService.getCustomerSalesReport(customerId, startDate, endDate)));
    }

    @GetMapping("/sales/product/{productId}")
    public ResponseEntity<ApiResponse<ProductSalesReportResponse>> getProductSalesReport(
            @PathVariable Long productId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate == null)
            startDate = LocalDate.now().minusMonths(3);
        if (endDate == null)
            endDate = LocalDate.now();
        return ResponseEntity.ok(ApiResponse.success(
                reportsService.getProductSalesReport(productId, startDate, endDate)));
    }

    @GetMapping("/production")
    public ResponseEntity<ApiResponse<DailyProductionReportResponse>> getProductionReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate == null)
            startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null)
            endDate = LocalDate.now();
        return ResponseEntity.ok(ApiResponse.success(
                reportsService.getDailyProductionReport(startDate, endDate)));
    }

    @GetMapping("/profit-loss")
    public ResponseEntity<ApiResponse<ProfitLossReportResponse>> getProfitLossReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate == null)
            startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null)
            endDate = LocalDate.now();
        return ResponseEntity.ok(ApiResponse.success(
                reportsService.getProfitLossReport(startDate, endDate)));
    }
}