package com.brickerp.reports.service;

import com.brickerp.reports.dto.GstReportResponse;
import java.time.LocalDate;

public interface ReportsService {
    GstReportResponse getGstReport(LocalDate startDate, LocalDate endDate);
}