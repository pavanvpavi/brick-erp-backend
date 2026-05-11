package com.brickerp.dashboard.service;

import com.brickerp.dashboard.dto.*;
import java.time.LocalDate;

public interface DashboardService {
    DashboardStatsResponse getDashboardStats();

    SalesReportResponse getSalesReport(LocalDate startDate, LocalDate endDate);

    InventoryReportResponse getInventoryReport();

    FinanceReportResponse getFinanceReport();
}