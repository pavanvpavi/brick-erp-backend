package com.brickerp.reports.service;

import com.brickerp.reports.dto.*;
import java.time.LocalDate;

public interface ReportsService {
    GstReportResponse getGstReport(LocalDate startDate, LocalDate endDate);

    SalesReportDetailResponse getSalesReport(LocalDate startDate, LocalDate endDate);

    CustomerSalesReportResponse getCustomerSalesReport(Long customerId,
            LocalDate startDate, LocalDate endDate);

    ProductSalesReportResponse getProductSalesReport(Long productId,
            LocalDate startDate, LocalDate endDate);

    DailyProductionReportResponse getDailyProductionReport(
            LocalDate startDate, LocalDate endDate);

    ProfitLossReportResponse getProfitLossReport(
            LocalDate startDate, LocalDate endDate);
}