package com.brickerp.reports.service.impl;

import com.brickerp.finance.entity.Invoice;
import com.brickerp.finance.repository.InvoiceRepository;
import com.brickerp.reports.dto.GstReportResponse;
import com.brickerp.reports.dto.GstReportResponse.GstInvoiceEntry;
import com.brickerp.reports.service.ReportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportsServiceImpl implements ReportsService {

    private final InvoiceRepository invoiceRepository;

    @Override
    public GstReportResponse getGstReport(LocalDate startDate, LocalDate endDate) {
        List<Invoice> invoices = invoiceRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .filter(inv -> inv.getStatus() != Invoice.InvoiceStatus.CANCELLED
                        && !inv.getInvoiceDate().isBefore(startDate)
                        && !inv.getInvoiceDate().isAfter(endDate))
                .collect(Collectors.toList());

        BigDecimal totalTaxableValue = BigDecimal.ZERO;
        BigDecimal totalGst = BigDecimal.ZERO;
        BigDecimal totalInvoiceValue = BigDecimal.ZERO;

        List<GstInvoiceEntry> entries = invoices.stream().map(inv -> {
            // Taxable value = subtotal - discount
            BigDecimal taxableValue = inv.getSubtotal().subtract(inv.getDiscountAmount());
            BigDecimal gstAmount = inv.getTaxAmount();

            // Split GST into CGST and SGST (equal halves for intra-state)
            BigDecimal cgst = gstAmount.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
            BigDecimal sgst = gstAmount.subtract(cgst);

            // Calculate tax rate
            BigDecimal taxRate = taxableValue.compareTo(BigDecimal.ZERO) > 0
                    ? gstAmount.multiply(BigDecimal.valueOf(100))
                            .divide(taxableValue, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            return GstInvoiceEntry.builder()
                    .invoiceNumber(inv.getInvoiceNumber())
                    .invoiceDate(inv.getInvoiceDate().toString())
                    .customerName(inv.getCustomer().getName())
                    .customerGstin(inv.getCustomer().getGstin() != null
                            ? inv.getCustomer().getGstin()
                            : "UNREGISTERED")
                    .taxableValue(taxableValue)
                    .taxRate(taxRate)
                    .cgst(cgst)
                    .sgst(sgst)
                    .igst(BigDecimal.ZERO)
                    .totalGst(gstAmount)
                    .invoiceValue(inv.getTotalAmount())
                    .build();
        }).collect(Collectors.toList());

        totalTaxableValue = entries.stream()
                .map(GstInvoiceEntry::getTaxableValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalGst = entries.stream()
                .map(GstInvoiceEntry::getTotalGst)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalInvoiceValue = entries.stream()
                .map(GstInvoiceEntry::getInvoiceValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCgst = totalGst.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        BigDecimal totalSgst = totalGst.subtract(totalCgst);

        return GstReportResponse.builder()
                .period(startDate + " to " + endDate)
                .totalTaxableValue(totalTaxableValue)
                .totalCgst(totalCgst)
                .totalSgst(totalSgst)
                .totalIgst(BigDecimal.ZERO)
                .totalGst(totalGst)
                .totalInvoiceValue(totalInvoiceValue)
                .totalInvoices((long) invoices.size())
                .invoices(entries)
                .build();
    }
}