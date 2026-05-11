package com.brickerp.dashboard.controller;

import com.brickerp.common.response.ApiResponse;
import com.brickerp.dashboard.dto.*;
import com.brickerp.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(
                dashboardService.getDashboardStats()));
    }

    @GetMapping("/reports/sales")
    public ResponseEntity<ApiResponse<SalesReportResponse>> getSalesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate == null)
            startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null)
            endDate = LocalDate.now();

        return ResponseEntity.ok(ApiResponse.success(
                dashboardService.getSalesReport(startDate, endDate)));
    }

    @GetMapping("/reports/inventory")
    public ResponseEntity<ApiResponse<InventoryReportResponse>> getInventoryReport() {
        return ResponseEntity.ok(ApiResponse.success(
                dashboardService.getInventoryReport()));
    }

    @GetMapping("/reports/finance")
    public ResponseEntity<ApiResponse<FinanceReportResponse>> getFinanceReport() {
        return ResponseEntity.ok(ApiResponse.success(
                dashboardService.getFinanceReport()));
    }
}