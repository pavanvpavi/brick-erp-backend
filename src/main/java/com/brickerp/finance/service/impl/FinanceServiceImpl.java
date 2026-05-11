package com.brickerp.finance.service.impl;

import com.brickerp.common.exception.BusinessException;
import com.brickerp.common.exception.ResourceNotFoundException;
import com.brickerp.customer.entity.Customer;
import com.brickerp.customer.repository.CustomerRepository;
import com.brickerp.finance.dto.*;
import com.brickerp.finance.entity.Invoice;
import com.brickerp.finance.entity.Invoice.InvoiceStatus;
import com.brickerp.finance.entity.InvoiceItem;
import com.brickerp.finance.entity.Payment;
import com.brickerp.finance.entity.Payment.PaymentMethod;
import com.brickerp.finance.repository.InvoiceRepository;
import com.brickerp.finance.repository.PaymentRepository;
import com.brickerp.finance.service.FinanceService;
import com.brickerp.order.entity.SalesOrder;
import com.brickerp.order.entity.SalesOrderItem;
import com.brickerp.order.repository.SalesOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FinanceServiceImpl implements FinanceService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final CustomerRepository customerRepository;

    @Override
    public InvoiceResponse createInvoiceFromOrder(CreateInvoiceFromOrderRequest request) {
        SalesOrder order = salesOrderRepository.findById(request.getSalesOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder",
                        request.getSalesOrderId()));

        if (order.getStatus() == SalesOrder.OrderStatus.DRAFT) {
            throw new BusinessException("Cannot create invoice for a DRAFT order. Confirm the order first.");
        }
        if (order.getStatus() == SalesOrder.OrderStatus.CANCELLED) {
            throw new BusinessException("Cannot create invoice for a CANCELLED order.");
        }

        // Check if invoice already exists for this order
        if (invoiceRepository.findBySalesOrderId(order.getId()).isPresent()) {
            throw new BusinessException("Invoice already exists for order: " + order.getOrderNumber());
        }

        LocalDate dueDate = request.getDueDate() != null
                ? request.getDueDate()
                : LocalDate.now().plusDays(
                        order.getCustomer().getCreditDays() != null
                                ? order.getCustomer().getCreditDays()
                                : 30);

        Invoice invoice = Invoice.builder()
                .invoiceNumber(generateInvoiceNumber())
                .customer(order.getCustomer())
                .salesOrder(order)
                .invoiceDate(LocalDate.now())
                .dueDate(dueDate)
                .subtotal(order.getSubtotal())
                .taxAmount(order.getTaxAmount())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .paidAmount(java.math.BigDecimal.ZERO)
                .status(InvoiceStatus.DRAFT)
                .notes(request.getNotes())
                .build();

        // Copy items from sales order
        for (SalesOrderItem orderItem : order.getItems()) {
            InvoiceItem item = InvoiceItem.builder()
                    .invoice(invoice)
                    .product(orderItem.getProduct())
                    .description(orderItem.getProduct().getName())
                    .quantity(orderItem.getQuantity())
                    .unitPrice(orderItem.getUnitPrice())
                    .taxPercentage(orderItem.getTaxPercentage())
                    .taxAmount(orderItem.getTaxAmount())
                    .discountAmount(orderItem.getDiscountAmount())
                    .lineTotal(orderItem.getLineTotal())
                    .build();
            invoice.getItems().add(item);
        }

        return toInvoiceResponse(invoiceRepository.save(invoice));
    }

    @Override
    public InvoiceResponse sendInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", id));

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new BusinessException("Only DRAFT invoices can be sent");
        }

        invoice.setStatus(InvoiceStatus.SENT);
        return toInvoiceResponse(invoiceRepository.save(invoice));
    }

    @Override
    public InvoiceResponse recordPayment(Long invoiceId, RecordPaymentRequest request) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", invoiceId));

        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new BusinessException("Cannot record payment for a cancelled invoice");
        }
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BusinessException("Invoice is already fully paid");
        }

        if (request.getAmount().compareTo(invoice.getBalanceDue()) > 0) {
            throw new BusinessException("Payment amount (" + request.getAmount()
                    + ") exceeds balance due (" + invoice.getBalanceDue() + ")");
        }

        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid payment method: " + request.getPaymentMethod());
        }

        Payment payment = Payment.builder()
                .paymentNumber(generatePaymentNumber())
                .invoice(invoice)
                .amount(request.getAmount())
                .paymentDate(request.getPaymentDate())
                .paymentMethod(method)
                .referenceNumber(request.getReferenceNumber())
                .notes(request.getNotes())
                .build();

        invoice.getPayments().add(payment);
        invoice.setPaidAmount(invoice.getPaidAmount().add(request.getAmount()));

        // Update invoice status
        if (invoice.getBalanceDue().compareTo(java.math.BigDecimal.ZERO) == 0) {
            invoice.setStatus(InvoiceStatus.PAID);
        } else {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }

        return toInvoiceResponse(invoiceRepository.save(invoice));
    }

    @Override
    public InvoiceResponse cancelInvoice(Long id, String reason) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", id));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BusinessException("Cannot cancel a fully paid invoice");
        }
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new BusinessException("Invoice is already cancelled");
        }

        invoice.setStatus(InvoiceStatus.CANCELLED);
        invoice.setNotes(reason != null ? reason : invoice.getNotes());
        return toInvoiceResponse(invoiceRepository.save(invoice));
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .map(this::toInvoiceResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", id));
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceByNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .map(this::toInvoiceResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice not found with number: " + invoiceNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceSummaryResponse> getAllInvoices() {
        return invoiceRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toSummaryResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceSummaryResponse> getInvoicesByCustomer(Long customerId) {
        return invoiceRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream().map(this::toSummaryResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceSummaryResponse> getInvoicesByStatus(String status) {
        try {
            InvoiceStatus invoiceStatus = InvoiceStatus.valueOf(status.toUpperCase());
            return invoiceRepository.findByStatusOrderByCreatedAtDesc(invoiceStatus)
                    .stream().map(this::toSummaryResponse).collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid status: " + status);
        }
    }

    // ==================== PRIVATE HELPERS ====================

    private String generateInvoiceNumber() {
        return invoiceRepository.findLastInvoiceNumber()
                .map(last -> {
                    int num = Integer.parseInt(last.replace("INV-", ""));
                    return String.format("INV-%05d", num + 1);
                })
                .orElse("INV-00001");
    }

    private String generatePaymentNumber() {
        return paymentRepository.findLastPaymentNumber()
                .map(last -> {
                    int num = Integer.parseInt(last.replace("PAY-", ""));
                    return String.format("PAY-%05d", num + 1);
                })
                .orElse("PAY-00001");
    }

    private InvoiceResponse toInvoiceResponse(Invoice i) {
        InvoiceResponse r = new InvoiceResponse();
        r.setId(i.getId());
        r.setInvoiceNumber(i.getInvoiceNumber());
        r.setCustomerId(i.getCustomer().getId());
        r.setCustomerName(i.getCustomer().getName());
        r.setCustomerCode(i.getCustomer().getCustomerCode());
        if (i.getSalesOrder() != null) {
            r.setSalesOrderId(i.getSalesOrder().getId());
            r.setSalesOrderNumber(i.getSalesOrder().getOrderNumber());
        }
        r.setStatus(i.getStatus().name());
        r.setInvoiceDate(i.getInvoiceDate());
        r.setDueDate(i.getDueDate());
        r.setSubtotal(i.getSubtotal());
        r.setTaxAmount(i.getTaxAmount());
        r.setDiscountAmount(i.getDiscountAmount());
        r.setTotalAmount(i.getTotalAmount());
        r.setPaidAmount(i.getPaidAmount());
        r.setBalanceDue(i.getBalanceDue());
        r.setNotes(i.getNotes());
        r.setCreatedAt(i.getCreatedAt());
        r.setUpdatedAt(i.getUpdatedAt());
        r.setItems(i.getItems().stream()
                .map(this::toItemResponse).collect(Collectors.toList()));
        r.setPayments(i.getPayments().stream()
                .map(this::toPaymentResponse).collect(Collectors.toList()));
        return r;
    }

    private InvoiceSummaryResponse toSummaryResponse(Invoice i) {
        InvoiceSummaryResponse r = new InvoiceSummaryResponse();
        r.setId(i.getId());
        r.setInvoiceNumber(i.getInvoiceNumber());
        r.setCustomerName(i.getCustomer().getName());
        r.setStatus(i.getStatus().name());
        r.setInvoiceDate(i.getInvoiceDate());
        r.setDueDate(i.getDueDate());
        r.setTotalAmount(i.getTotalAmount());
        r.setPaidAmount(i.getPaidAmount());
        r.setBalanceDue(i.getBalanceDue());
        return r;
    }

    private InvoiceItemResponse toItemResponse(InvoiceItem item) {
        InvoiceItemResponse r = new InvoiceItemResponse();
        r.setId(item.getId());
        r.setProductId(item.getProduct().getId());
        r.setProductName(item.getProduct().getName());
        r.setProductSku(item.getProduct().getSku());
        r.setDescription(item.getDescription());
        r.setQuantity(item.getQuantity());
        r.setUnitPrice(item.getUnitPrice());
        r.setTaxPercentage(item.getTaxPercentage());
        r.setTaxAmount(item.getTaxAmount());
        r.setDiscountAmount(item.getDiscountAmount());
        r.setLineTotal(item.getLineTotal());
        return r;
    }

    private PaymentResponse toPaymentResponse(Payment p) {
        PaymentResponse r = new PaymentResponse();
        r.setId(p.getId());
        r.setPaymentNumber(p.getPaymentNumber());
        r.setAmount(p.getAmount());
        r.setPaymentDate(p.getPaymentDate());
        r.setPaymentMethod(p.getPaymentMethod().name());
        r.setReferenceNumber(p.getReferenceNumber());
        r.setNotes(p.getNotes());
        r.setCreatedAt(p.getCreatedAt());
        return r;
    }
}