package com.brickerp.dashboard.controller;

import com.brickerp.common.response.ApiResponse;
import com.brickerp.dashboard.dto.*;
import com.brickerp.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import com.brickerp.common.config.EmailService;
import com.brickerp.inventory.repository.StockRepository;
import com.brickerp.inventory.entity.Stock;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    private final EmailService emailService;
    private final StockRepository stockRepository;

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

    @PostMapping("/test-email")
    public ResponseEntity<ApiResponse<String>> testEmail() {
        try {
            emailService.sendEmail(
                    System.getenv("ALERT_EMAIL"),
                    "✅ Brick ERP - Email Test",
                    "Email alerts are working correctly!\n\nBrick ERP System");
            return ResponseEntity.ok(ApiResponse.success("Test email sent successfully", "OK"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("Email failed: " + e.getMessage()));
        }
    }

    @PostMapping("/trigger-low-stock-check")
    public ResponseEntity<ApiResponse<String>> triggerLowStockCheck() {
        List<Stock> lowStockItems = stockRepository.findLowStockItems();
        if (lowStockItems.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("No low stock items found", "OK"));
        }
        for (Stock stock : lowStockItems) {
            emailService.sendLowStockAlert(
                    stock.getProduct().getName(),
                    stock.getWarehouse().getName(),
                    stock.getQuantityOnHand(),
                    stock.getProduct().getMinimumStockLevel());
        }
        return ResponseEntity.ok(ApiResponse.success(
                "Low stock alerts sent for " + lowStockItems.size() + " items", "OK"));
    }
}