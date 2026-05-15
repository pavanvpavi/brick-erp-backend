package com.brickerp.reports.controller;

import com.brickerp.common.response.ApiResponse;
import com.brickerp.reports.dto.GstReportResponse;
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
}