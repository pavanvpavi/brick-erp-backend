package com.brickerp.finance.service;

import com.brickerp.finance.dto.*;
import java.util.List;

public interface FinanceService {
    InvoiceResponse createInvoiceFromOrder(CreateInvoiceFromOrderRequest request);

    InvoiceResponse getInvoiceById(Long id);

    InvoiceResponse getInvoiceByNumber(String invoiceNumber);

    List<InvoiceSummaryResponse> getAllInvoices();

    List<InvoiceSummaryResponse> getInvoicesByCustomer(Long customerId);

    List<InvoiceSummaryResponse> getInvoicesByStatus(String status);

    InvoiceResponse sendInvoice(Long id);

    InvoiceResponse recordPayment(Long invoiceId, RecordPaymentRequest request);

    InvoiceResponse cancelInvoice(Long id, String reason);
}