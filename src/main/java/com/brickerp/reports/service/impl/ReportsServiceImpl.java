package com.brickerp.reports.service.impl;

import com.brickerp.common.exception.ResourceNotFoundException;
import com.brickerp.customer.entity.Customer;
import com.brickerp.customer.repository.CustomerRepository;
import com.brickerp.finance.entity.Expense;
import com.brickerp.finance.entity.Invoice;
import com.brickerp.finance.repository.ExpenseRepository;
import com.brickerp.finance.repository.InvoiceRepository;
import com.brickerp.manufacturing.entity.ProductionOrder;
import com.brickerp.manufacturing.repository.ProductionOrderRepository;
import com.brickerp.order.entity.SalesOrder;
import com.brickerp.order.entity.SalesOrderItem;
import com.brickerp.order.repository.SalesOrderRepository;
import com.brickerp.product.entity.Product;
import com.brickerp.product.repository.ProductRepository;
import com.brickerp.reports.dto.*;
import com.brickerp.reports.dto.CustomerSalesReportResponse.*;
import com.brickerp.reports.dto.DailyProductionReportResponse.*;
import com.brickerp.reports.dto.ProfitLossReportResponse.*;
import com.brickerp.reports.dto.ProductSalesReportResponse.*;
import com.brickerp.reports.dto.SalesReportDetailResponse.*;
import com.brickerp.reports.dto.GstReportResponse.*;
import com.brickerp.reports.service.ReportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportsServiceImpl implements ReportsService {

        private final SalesOrderRepository salesOrderRepository;
        private final InvoiceRepository invoiceRepository;
        private final ExpenseRepository expenseRepository;
        private final ProductionOrderRepository productionOrderRepository;
        private final CustomerRepository customerRepository;
        private final ProductRepository productRepository;

        // ==================== GST REPORT ====================

        @Override
        public GstReportResponse getGstReport(LocalDate startDate, LocalDate endDate) {
                List<Invoice> invoices = invoiceRepository.findAllByOrderByCreatedAtDesc()
                                .stream()
                                .filter(inv -> inv.getStatus() != Invoice.InvoiceStatus.CANCELLED
                                                && !inv.getInvoiceDate().isBefore(startDate)
                                                && !inv.getInvoiceDate().isAfter(endDate))
                                .collect(Collectors.toList());

                List<GstInvoiceEntry> entries = invoices.stream().map(inv -> {
                        BigDecimal taxableValue = inv.getSubtotal().subtract(inv.getDiscountAmount());
                        BigDecimal gstAmount = inv.getTaxAmount();
                        BigDecimal cgst = gstAmount.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
                        BigDecimal sgst = gstAmount.subtract(cgst);
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
                                        .cgst(cgst).sgst(sgst)
                                        .igst(BigDecimal.ZERO)
                                        .totalGst(gstAmount)
                                        .invoiceValue(inv.getTotalAmount())
                                        .build();
                }).collect(Collectors.toList());

                BigDecimal totalTaxableValue = entries.stream()
                                .map(GstInvoiceEntry::getTaxableValue)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalGst = entries.stream()
                                .map(GstInvoiceEntry::getTotalGst)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalInvoiceValue = entries.stream()
                                .map(GstInvoiceEntry::getInvoiceValue)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalCgst = totalGst.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);

                return GstReportResponse.builder()
                                .period(startDate + " to " + endDate)
                                .totalTaxableValue(totalTaxableValue)
                                .totalCgst(totalCgst)
                                .totalSgst(totalGst.subtract(totalCgst))
                                .totalIgst(BigDecimal.ZERO)
                                .totalGst(totalGst)
                                .totalInvoiceValue(totalInvoiceValue)
                                .totalInvoices((long) invoices.size())
                                .invoices(entries)
                                .build();
        }

        // ==================== SALES REPORT ====================

        @Override
        public SalesReportDetailResponse getSalesReport(LocalDate startDate, LocalDate endDate) {
                List<SalesOrder> orders = salesOrderRepository.findByDateRange(startDate, endDate);

                BigDecimal totalRevenue = BigDecimal.ZERO;
                BigDecimal totalTax = BigDecimal.ZERO;
                BigDecimal totalDiscount = BigDecimal.ZERO;
                long totalItemsSold = 0;

                Map<String, BigDecimal> monthlyRevenue = new LinkedHashMap<>();
                Map<String, Long> monthlyOrders = new LinkedHashMap<>();
                Map<String, Long> monthlyItems = new LinkedHashMap<>();

                List<SalesOrderEntry> orderEntries = new ArrayList<>();

                for (SalesOrder order : orders) {
                        if (order.getStatus() == SalesOrder.OrderStatus.CANCELLED)
                                continue;

                        totalRevenue = totalRevenue.add(order.getTotalAmount());
                        totalTax = totalTax.add(order.getTaxAmount());
                        totalDiscount = totalDiscount.add(order.getDiscountAmount());

                        long itemCount = order.getItems().stream()
                                        .mapToLong(SalesOrderItem::getQuantity).sum();
                        totalItemsSold += itemCount;

                        String month = order.getOrderDate()
                                        .format(DateTimeFormatter.ofPattern("yyyy-MM"));
                        monthlyRevenue.merge(month, order.getTotalAmount(), BigDecimal::add);
                        monthlyOrders.merge(month, 1L, Long::sum);
                        monthlyItems.merge(month, itemCount, Long::sum);

                        orderEntries.add(SalesOrderEntry.builder()
                                        .orderNumber(order.getOrderNumber())
                                        .orderDate(order.getOrderDate().toString())
                                        .customerName(order.getCustomer().getName())
                                        .status(order.getStatus().name())
                                        .itemCount(order.getItems().size())
                                        .subtotal(order.getSubtotal())
                                        .taxAmount(order.getTaxAmount())
                                        .discountAmount(order.getDiscountAmount())
                                        .totalAmount(order.getTotalAmount())
                                        .build());
                }

                List<MonthlySummary> monthlySummaries = monthlyRevenue.entrySet().stream()
                                .map(e -> MonthlySummary.builder()
                                                .month(e.getKey())
                                                .revenue(e.getValue())
                                                .orderCount(monthlyOrders.getOrDefault(e.getKey(), 0L))
                                                .itemsSold(monthlyItems.getOrDefault(e.getKey(), 0L))
                                                .build())
                                .collect(Collectors.toList());

                long cancelled = orders.stream()
                                .filter(o -> o.getStatus() == SalesOrder.OrderStatus.CANCELLED).count();
                long confirmed = orders.stream()
                                .filter(o -> o.getStatus() != SalesOrder.OrderStatus.CANCELLED).count();

                return SalesReportDetailResponse.builder()
                                .period(startDate + " to " + endDate)
                                .totalOrders((long) orders.size())
                                .confirmedOrders(confirmed)
                                .cancelledOrders(cancelled)
                                .totalRevenue(totalRevenue)
                                .totalTax(totalTax)
                                .totalDiscount(totalDiscount)
                                .netRevenue(totalRevenue)
                                .totalItemsSold(totalItemsSold)
                                .orders(orderEntries)
                                .monthlySummary(monthlySummaries)
                                .build();
        }

        // ==================== CUSTOMER SALES REPORT ====================

        @Override
        public CustomerSalesReportResponse getCustomerSalesReport(
                        Long customerId, LocalDate startDate, LocalDate endDate) {

                Customer customer = customerRepository.findById(customerId)
                                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));

                List<SalesOrder> orders = salesOrderRepository
                                .findByCustomerIdOrderByCreatedAtDesc(customerId)
                                .stream()
                                .filter(o -> !o.getOrderDate().isBefore(startDate)
                                                && !o.getOrderDate().isAfter(endDate))
                                .collect(Collectors.toList());

                List<Invoice> invoices = invoiceRepository
                                .findByCustomerIdOrderByCreatedAtDesc(customerId);

                Map<Long, Invoice> orderInvoiceMap = invoices.stream()
                                .filter(inv -> inv.getSalesOrder() != null)
                                .collect(Collectors.toMap(
                                                inv -> inv.getSalesOrder().getId(),
                                                inv -> inv,
                                                (a, b) -> a));

                BigDecimal totalRevenue = BigDecimal.ZERO;
                BigDecimal totalPaid = BigDecimal.ZERO;
                long totalItems = 0;

                Map<Long, Long> productQtyMap = new HashMap<>();
                Map<Long, BigDecimal> productRevenueMap = new HashMap<>();
                Map<Long, String> productNameMap = new HashMap<>();
                Map<Long, String> productSkuMap = new HashMap<>();

                List<CustomerOrderEntry> orderEntries = new ArrayList<>();

                for (SalesOrder order : orders) {
                        if (order.getStatus() == SalesOrder.OrderStatus.CANCELLED)
                                continue;
                        totalRevenue = totalRevenue.add(order.getTotalAmount());
                        totalItems += order.getItems().stream()
                                        .mapToLong(SalesOrderItem::getQuantity).sum();

                        Invoice inv = orderInvoiceMap.get(order.getId());
                        String invoiceNumber = inv != null ? inv.getInvoiceNumber() : "—";
                        String invoiceStatus = inv != null ? inv.getStatus().name() : "NO INVOICE";
                        if (inv != null)
                                totalPaid = totalPaid.add(inv.getPaidAmount());

                        for (SalesOrderItem item : order.getItems()) {
                                Long pid = item.getProduct().getId();
                                productQtyMap.merge(pid, (long) item.getQuantity(), Long::sum);
                                productRevenueMap.merge(pid, item.getLineTotal(), BigDecimal::add);
                                productNameMap.put(pid, item.getProduct().getName());
                                productSkuMap.put(pid, item.getProduct().getSku());
                        }

                        orderEntries.add(CustomerOrderEntry.builder()
                                        .orderNumber(order.getOrderNumber())
                                        .orderDate(order.getOrderDate().toString())
                                        .status(order.getStatus().name())
                                        .itemCount(order.getItems().size())
                                        .totalAmount(order.getTotalAmount())
                                        .invoiceNumber(invoiceNumber)
                                        .invoiceStatus(invoiceStatus)
                                        .build());
                }

                List<TopProductEntry> topProducts = productQtyMap.entrySet().stream()
                                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                                .limit(5)
                                .map(e -> TopProductEntry.builder()
                                                .productName(productNameMap.get(e.getKey()))
                                                .productSku(productSkuMap.get(e.getKey()))
                                                .quantityPurchased(e.getValue())
                                                .totalAmount(productRevenueMap.get(e.getKey()))
                                                .build())
                                .collect(Collectors.toList());

                return CustomerSalesReportResponse.builder()
                                .customerId(customer.getId())
                                .customerName(customer.getName())
                                .customerCode(customer.getCustomerCode())
                                .period(startDate + " to " + endDate)
                                .totalOrders((long) orderEntries.size())
                                .totalRevenue(totalRevenue)
                                .totalPaid(totalPaid)
                                .totalOutstanding(totalRevenue.subtract(totalPaid))
                                .totalItemsPurchased(totalItems)
                                .orders(orderEntries)
                                .topProducts(topProducts)
                                .build();
        }

        // ==================== PRODUCT SALES REPORT ====================

        @Override
        public ProductSalesReportResponse getProductSalesReport(
                        Long productId, LocalDate startDate, LocalDate endDate) {

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

                List<SalesOrder> allOrders = salesOrderRepository.findByDateRange(startDate, endDate);

                List<ProductSaleEntry> salesEntries = new ArrayList<>();
                Map<String, Long> monthlyQty = new LinkedHashMap<>();
                Map<String, BigDecimal> monthlyRevenue = new LinkedHashMap<>();

                long totalQty = 0;
                BigDecimal totalRevenue = BigDecimal.ZERO;

                for (SalesOrder order : allOrders) {
                        if (order.getStatus() == SalesOrder.OrderStatus.CANCELLED)
                                continue;
                        for (SalesOrderItem item : order.getItems()) {
                                if (!item.getProduct().getId().equals(productId))
                                        continue;

                                totalQty += item.getQuantity();
                                totalRevenue = totalRevenue.add(item.getLineTotal());

                                String month = order.getOrderDate()
                                                .format(DateTimeFormatter.ofPattern("yyyy-MM"));
                                monthlyQty.merge(month, (long) item.getQuantity(), Long::sum);
                                monthlyRevenue.merge(month, item.getLineTotal(), BigDecimal::add);

                                salesEntries.add(ProductSaleEntry.builder()
                                                .orderNumber(order.getOrderNumber())
                                                .orderDate(order.getOrderDate().toString())
                                                .customerName(order.getCustomer().getName())
                                                .quantity(item.getQuantity())
                                                .unitPrice(item.getUnitPrice())
                                                .discountAmount(item.getDiscountAmount())
                                                .lineTotal(item.getLineTotal())
                                                .build());
                        }
                }

                BigDecimal avgPrice = totalQty > 0
                                ? totalRevenue.divide(BigDecimal.valueOf(totalQty), 2, RoundingMode.HALF_UP)
                                : BigDecimal.ZERO;

                BigDecimal costPrice = product.getCostPrice() != null
                                ? product.getCostPrice().multiply(BigDecimal.valueOf(totalQty))
                                : BigDecimal.ZERO;

                BigDecimal grossProfit = totalRevenue.subtract(costPrice);

                List<MonthlySalesSummary> monthlySummaries = monthlyQty.entrySet().stream()
                                .map(e -> MonthlySalesSummary.builder()
                                                .month(e.getKey())
                                                .quantitySold(e.getValue())
                                                .revenue(monthlyRevenue.getOrDefault(e.getKey(), BigDecimal.ZERO))
                                                .build())
                                .collect(Collectors.toList());

                return ProductSalesReportResponse.builder()
                                .productId(product.getId())
                                .productName(product.getName())
                                .productSku(product.getSku())
                                .period(startDate + " to " + endDate)
                                .totalOrdersContaining((long) salesEntries.size())
                                .totalQuantitySold(totalQty)
                                .totalRevenue(totalRevenue)
                                .averageSellingPrice(avgPrice)
                                .costPrice(costPrice)
                                .grossProfit(grossProfit)
                                .salesHistory(salesEntries)
                                .monthlySummary(monthlySummaries)
                                .build();
        }

        // ==================== DAILY PRODUCTION REPORT ====================

        @Override
        public DailyProductionReportResponse getDailyProductionReport(
                        LocalDate startDate, LocalDate endDate) {

                List<ProductionOrder> orders = productionOrderRepository.findAll()
                                .stream()
                                .filter(o -> {
                                        LocalDate date = o.getActualStartDate() != null
                                                        ? o.getActualStartDate()
                                                        : o.getPlannedStartDate();
                                        return date != null
                                                        && !date.isBefore(startDate)
                                                        && !date.isAfter(endDate);
                                })
                                .collect(Collectors.toList());

                Map<String, Integer> productProduced = new LinkedHashMap<>();
                Map<String, Long> productOrderCount = new LinkedHashMap<>();

                int totalPlanned = 0;
                int totalProduced = 0;

                List<ProductionEntry> entries = new ArrayList<>();

                for (ProductionOrder o : orders) {
                        totalPlanned += o.getPlannedQuantity();
                        totalProduced += o.getProducedQuantity();

                        String productName = o.getFinishedProduct().getName();
                        productProduced.merge(productName, o.getProducedQuantity(), Integer::sum);
                        productOrderCount.merge(productName, 1L, Long::sum);

                        entries.add(ProductionEntry.builder()
                                        .productionNumber(o.getProductionNumber())
                                        .productName(productName)
                                        .status(o.getStatus().name())
                                        .plannedQuantity(o.getPlannedQuantity())
                                        .producedQuantity(o.getProducedQuantity())
                                        .plannedStartDate(o.getPlannedStartDate() != null
                                                        ? o.getPlannedStartDate().toString()
                                                        : "—")
                                        .actualStartDate(o.getActualStartDate() != null
                                                        ? o.getActualStartDate().toString()
                                                        : "—")
                                        .actualEndDate(o.getActualEndDate() != null
                                                        ? o.getActualEndDate().toString()
                                                        : "—")
                                        .warehouseName(o.getWarehouseId().toString())
                                        .build());
                }

                List<ProductSummary> productSummaries = productProduced.entrySet().stream()
                                .map(e -> ProductSummary.builder()
                                                .productName(e.getKey())
                                                .totalProduced(e.getValue())
                                                .orderCount(productOrderCount.getOrDefault(e.getKey(), 0L))
                                                .build())
                                .sorted(Comparator.comparingInt(ProductSummary::getTotalProduced).reversed())
                                .collect(Collectors.toList());

                double completionRate = totalPlanned > 0
                                ? (double) totalProduced / totalPlanned * 100
                                : 0;

                long completed = orders.stream()
                                .filter(o -> o.getStatus() == ProductionOrder.ProductionStatus.COMPLETED)
                                .count();
                long inProgress = orders.stream()
                                .filter(o -> o.getStatus() == ProductionOrder.ProductionStatus.IN_PROGRESS)
                                .count();
                long planned = orders.stream()
                                .filter(o -> o.getStatus() == ProductionOrder.ProductionStatus.PLANNED)
                                .count();
                long cancelled = orders.stream()
                                .filter(o -> o.getStatus() == ProductionOrder.ProductionStatus.CANCELLED)
                                .count();

                return DailyProductionReportResponse.builder()
                                .period(startDate + " to " + endDate)
                                .totalProductionOrders((long) orders.size())
                                .completedOrders(completed)
                                .inProgressOrders(inProgress)
                                .plannedOrders(planned)
                                .cancelledOrders(cancelled)
                                .totalPlannedQuantity(totalPlanned)
                                .totalProducedQuantity(totalProduced)
                                .completionRate(Math.round(completionRate * 10.0) / 10.0)
                                .productionOrders(entries)
                                .productSummary(productSummaries)
                                .build();
        }

        // ==================== PROFIT & LOSS REPORT ====================

        @Override
        public ProfitLossReportResponse getProfitLossReport(
                        LocalDate startDate, LocalDate endDate) {

                List<SalesOrder> orders = salesOrderRepository.findByDateRange(startDate, endDate)
                                .stream()
                                .filter(o -> o.getStatus() != SalesOrder.OrderStatus.CANCELLED)
                                .collect(Collectors.toList());

                List<Invoice> invoices = invoiceRepository.findAllByOrderByCreatedAtDesc()
                                .stream()
                                .filter(inv -> inv.getStatus() != Invoice.InvoiceStatus.CANCELLED
                                                && !inv.getInvoiceDate().isBefore(startDate)
                                                && !inv.getInvoiceDate().isAfter(endDate))
                                .collect(Collectors.toList());

                List<Expense> expenses = expenseRepository.findByDateRange(startDate, endDate);

                // Revenue
                BigDecimal totalRevenue = orders.stream()
                                .map(SalesOrder::getTotalAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalTax = orders.stream()
                                .map(SalesOrder::getTaxAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalDiscount = orders.stream()
                                .map(SalesOrder::getDiscountAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal netRevenue = totalRevenue.subtract(totalTax);

                // Cost of Goods Sold
                BigDecimal cogs = orders.stream()
                                .flatMap(o -> o.getItems().stream())
                                .map(item -> {
                                        BigDecimal cost = item.getProduct().getCostPrice() != null
                                                        ? item.getProduct().getCostPrice()
                                                        : BigDecimal.ZERO;
                                        return cost.multiply(BigDecimal.valueOf(item.getQuantity()));
                                })
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal grossProfit = netRevenue.subtract(cogs);
                BigDecimal grossProfitMargin = netRevenue.compareTo(BigDecimal.ZERO) > 0
                                ? grossProfit.multiply(BigDecimal.valueOf(100))
                                                .divide(netRevenue, 2, RoundingMode.HALF_UP)
                                : BigDecimal.ZERO;

                // Expenses breakdown
                BigDecimal totalExpenses = expenses.stream()
                                .map(Expense::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                Map<String, BigDecimal> expenseByCategory = new LinkedHashMap<>();
                for (Expense expense : expenses) {
                        expenseByCategory.merge(
                                        expense.getCategory().name(),
                                        expense.getAmount(),
                                        BigDecimal::add);
                }

                List<ExpenseCategorySummary> expenseBreakdown = expenseByCategory.entrySet().stream()
                                .map(e -> ExpenseCategorySummary.builder()
                                                .category(e.getKey())
                                                .amount(e.getValue())
                                                .percentage(totalExpenses.compareTo(BigDecimal.ZERO) > 0
                                                                ? e.getValue().multiply(BigDecimal.valueOf(100))
                                                                                .divide(totalExpenses, 1,
                                                                                                RoundingMode.HALF_UP)
                                                                                .doubleValue()
                                                                : 0.0)
                                                .build())
                                .sorted(Comparator.comparing(ExpenseCategorySummary::getAmount).reversed())
                                .collect(Collectors.toList());

                // Net profit
                BigDecimal netProfit = grossProfit.subtract(totalExpenses);
                BigDecimal netProfitMargin = netRevenue.compareTo(BigDecimal.ZERO) > 0
                                ? netProfit.multiply(BigDecimal.valueOf(100))
                                                .divide(netRevenue, 2, RoundingMode.HALF_UP)
                                : BigDecimal.ZERO;

                // Collections
                BigDecimal totalInvoiced = invoices.stream()
                                .map(Invoice::getTotalAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalCollected = invoices.stream()
                                .map(Invoice::getPaidAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                return ProfitLossReportResponse.builder()
                                .period(startDate + " to " + endDate)
                                .totalRevenue(totalRevenue)
                                .totalTaxCollected(totalTax)
                                .totalDiscountGiven(totalDiscount)
                                .netRevenue(netRevenue)
                                .costOfGoodsSold(cogs)
                                .grossProfit(grossProfit)
                                .grossProfitMargin(grossProfitMargin)
                                .totalExpenses(totalExpenses)
                                .expenseBreakdown(expenseBreakdown)
                                .netProfit(netProfit)
                                .netProfitMargin(netProfitMargin)
                                .totalInvoiced(totalInvoiced)
                                .totalCollected(totalCollected)
                                .totalOutstanding(totalInvoiced.subtract(totalCollected))
                                .build();
        }
}