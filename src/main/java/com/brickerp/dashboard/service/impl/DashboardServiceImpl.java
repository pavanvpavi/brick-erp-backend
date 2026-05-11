package com.brickerp.dashboard.service.impl;

import com.brickerp.customer.repository.CustomerRepository;
import com.brickerp.dashboard.dto.*;
import com.brickerp.dashboard.dto.SalesReportResponse.*;
import com.brickerp.dashboard.service.DashboardService;
import com.brickerp.finance.entity.Invoice;
import com.brickerp.finance.repository.InvoiceRepository;
import com.brickerp.inventory.entity.StockMovement;
import com.brickerp.inventory.repository.StockMovementRepository;
import com.brickerp.inventory.repository.StockRepository;
import com.brickerp.inventory.repository.WarehouseRepository;
import com.brickerp.manufacturing.entity.ProductionOrder;
import com.brickerp.manufacturing.repository.ProductionOrderRepository;
import com.brickerp.order.entity.SalesOrder;
import com.brickerp.order.entity.SalesOrderItem;
import com.brickerp.order.repository.SalesOrderRepository;
import com.brickerp.procurement.repository.PurchaseOrderRepository;
import com.brickerp.procurement.repository.SupplierRepository;
import com.brickerp.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

        private final SalesOrderRepository salesOrderRepository;
        private final ProductRepository productRepository;
        private final CustomerRepository customerRepository;
        private final InvoiceRepository invoiceRepository;
        private final StockRepository stockRepository;
        private final StockMovementRepository stockMovementRepository;
        private final WarehouseRepository warehouseRepository;
        private final SupplierRepository supplierRepository;
        private final PurchaseOrderRepository purchaseOrderRepository;
        private final ProductionOrderRepository productionOrderRepository;

        @Override
        public DashboardStatsResponse getDashboardStats() {

                // Sales
                List<SalesOrder> allOrders = salesOrderRepository.findAll();
                BigDecimal totalSales = allOrders.stream()
                                .filter(o -> o.getStatus() != SalesOrder.OrderStatus.CANCELLED)
                                .map(SalesOrder::getTotalAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                LocalDate firstOfMonth = LocalDate.now().withDayOfMonth(1);
                BigDecimal monthlySales = allOrders.stream()
                                .filter(o -> o.getStatus() != SalesOrder.OrderStatus.CANCELLED
                                                && !o.getOrderDate().isBefore(firstOfMonth))
                                .map(SalesOrder::getTotalAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                long pendingOrders = allOrders.stream()
                                .filter(o -> o.getStatus() == SalesOrder.OrderStatus.DRAFT
                                                || o.getStatus() == SalesOrder.OrderStatus.CONFIRMED
                                                || o.getStatus() == SalesOrder.OrderStatus.PROCESSING)
                                .count();

                long confirmedOrders = allOrders.stream()
                                .filter(o -> o.getStatus() == SalesOrder.OrderStatus.CONFIRMED)
                                .count();

                // Finance
                List<Invoice> allInvoices = invoiceRepository.findAll();
                BigDecimal totalOutstanding = allInvoices.stream()
                                .filter(i -> i.getStatus() != Invoice.InvoiceStatus.CANCELLED
                                                && i.getStatus() != Invoice.InvoiceStatus.PAID)
                                .map(Invoice::getBalanceDue)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalCollected = allInvoices.stream()
                                .map(Invoice::getPaidAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                long unpaidInvoices = allInvoices.stream()
                                .filter(i -> i.getStatus() == Invoice.InvoiceStatus.SENT
                                                || i.getStatus() == Invoice.InvoiceStatus.PARTIALLY_PAID
                                                || i.getStatus() == Invoice.InvoiceStatus.OVERDUE)
                                .count();

                // Manufacturing
                List<ProductionOrder> allProduction = productionOrderRepository.findAll();
                long activeProduction = allProduction.stream()
                                .filter(p -> p.getStatus() == ProductionOrder.ProductionStatus.IN_PROGRESS
                                                || p.getStatus() == ProductionOrder.ProductionStatus.PLANNED)
                                .count();

                long completedProduction = allProduction.stream()
                                .filter(p -> p.getStatus() == ProductionOrder.ProductionStatus.COMPLETED)
                                .count();

                // Procurement
                long pendingPOs = purchaseOrderRepository.findAll().stream()
                                .filter(po -> po.getStatus() != com.brickerp.procurement.entity.PurchaseOrder.PoStatus.RECEIVED
                                                && po.getStatus() != com.brickerp.procurement.entity.PurchaseOrder.PoStatus.CANCELLED)
                                .count();

                return DashboardStatsResponse.builder()
                                .totalOrders((long) allOrders.size())
                                .pendingOrders(pendingOrders)
                                .confirmedOrders(confirmedOrders)
                                .totalSalesAmount(totalSales)
                                .totalSalesThisMonth(monthlySales)
                                .totalProducts(productRepository.count())
                                .lowStockItems((long) stockRepository.findLowStockItems().size())
                                .totalWarehouses(warehouseRepository.count())
                                .totalInvoices((long) allInvoices.size())
                                .unpaidInvoices(unpaidInvoices)
                                .totalOutstanding(totalOutstanding)
                                .totalCollected(totalCollected)
                                .totalSuppliers(supplierRepository.count())
                                .pendingPurchaseOrders(pendingPOs)
                                .activeProductionOrders(activeProduction)
                                .completedProductionOrders(completedProduction)
                                .totalCustomers(customerRepository.count())
                                .activeCustomers((long) customerRepository.findByIsActiveTrue().size())
                                .build();
        }

        @Override
        public SalesReportResponse getSalesReport(LocalDate startDate, LocalDate endDate) {
                List<SalesOrder> orders = salesOrderRepository
                                .findByDateRange(startDate, endDate).stream()
                                .filter(o -> o.getStatus() != SalesOrder.OrderStatus.CANCELLED)
                                .collect(Collectors.toList());

                BigDecimal totalRevenue = orders.stream()
                                .map(SalesOrder::getTotalAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalTax = orders.stream()
                                .map(SalesOrder::getTaxAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalDiscount = orders.stream()
                                .map(SalesOrder::getDiscountAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                long totalItemsSold = orders.stream()
                                .flatMap(o -> o.getItems().stream())
                                .mapToLong(SalesOrderItem::getQuantity)
                                .sum();

                // Top products by quantity
                Map<Long, Long> productQtyMap = new HashMap<>();
                Map<Long, BigDecimal> productRevenueMap = new HashMap<>();
                Map<Long, String> productNameMap = new HashMap<>();
                Map<Long, String> productSkuMap = new HashMap<>();

                orders.forEach(order -> order.getItems().forEach(item -> {
                        Long pid = item.getProduct().getId();
                        productQtyMap.merge(pid, (long) item.getQuantity(), Long::sum);
                        productRevenueMap.merge(pid, item.getLineTotal(), BigDecimal::add);
                        productNameMap.put(pid, item.getProduct().getName());
                        productSkuMap.put(pid, item.getProduct().getSku());
                }));

                List<TopProductDto> topProducts = productQtyMap.entrySet().stream()
                                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                                .limit(5)
                                .map(e -> TopProductDto.builder()
                                                .productId(e.getKey())
                                                .productName(productNameMap.get(e.getKey()))
                                                .productSku(productSkuMap.get(e.getKey()))
                                                .quantitySold(e.getValue())
                                                .revenue(productRevenueMap.get(e.getKey()))
                                                .build())
                                .collect(Collectors.toList());

                // Top customers by amount
                Map<Long, BigDecimal> customerAmountMap = new HashMap<>();
                Map<Long, String> customerNameMap = new HashMap<>();
                Map<Long, String> customerCodeMap = new HashMap<>();
                Map<Long, Long> customerOrderCountMap = new HashMap<>();

                orders.forEach(order -> {
                        Long cid = order.getCustomer().getId();
                        customerAmountMap.merge(cid, order.getTotalAmount(), BigDecimal::add);
                        customerNameMap.put(cid, order.getCustomer().getName());
                        customerCodeMap.put(cid, order.getCustomer().getCustomerCode());
                        customerOrderCountMap.merge(cid, 1L, Long::sum);
                });

                List<TopCustomerDto> topCustomers = customerAmountMap.entrySet().stream()
                                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                                .limit(5)
                                .map(e -> TopCustomerDto.builder()
                                                .customerId(e.getKey())
                                                .customerName(customerNameMap.get(e.getKey()))
                                                .customerCode(customerCodeMap.get(e.getKey()))
                                                .orderCount(customerOrderCountMap.get(e.getKey()))
                                                .totalAmount(e.getValue())
                                                .build())
                                .collect(Collectors.toList());

                // Monthly sales breakdown
                Map<String, BigDecimal> monthlyRevenueMap = new LinkedHashMap<>();
                Map<String, Long> monthlyCountMap = new LinkedHashMap<>();

                orders.forEach(order -> {
                        String month = order.getOrderDate()
                                        .format(DateTimeFormatter.ofPattern("yyyy-MM"));
                        monthlyRevenueMap.merge(month, order.getTotalAmount(), BigDecimal::add);
                        monthlyCountMap.merge(month, 1L, Long::sum);
                });

                List<MonthlySalesDto> monthlySales = monthlyRevenueMap.entrySet().stream()
                                .map(e -> MonthlySalesDto.builder()
                                                .month(e.getKey())
                                                .revenue(e.getValue())
                                                .orderCount(monthlyCountMap.get(e.getKey()))
                                                .build())
                                .collect(Collectors.toList());

                return SalesReportResponse.builder()
                                .startDate(startDate)
                                .endDate(endDate)
                                .totalRevenue(totalRevenue)
                                .totalTax(totalTax)
                                .totalDiscount(totalDiscount)
                                .totalOrders((long) orders.size())
                                .totalItemsSold(totalItemsSold)
                                .topProducts(topProducts)
                                .topCustomers(topCustomers)
                                .monthlySales(monthlySales)
                                .build();
        }

        @Override
        public InventoryReportResponse getInventoryReport() {
                var allStocks = stockRepository.findAll();

                long lowStockCount = allStocks.stream()
                                .filter(s -> s.getQuantityOnHand() <= s.getProduct().getMinimumStockLevel()
                                                && s.getQuantityOnHand() > 0)
                                .count();

                long outOfStockCount = allStocks.stream()
                                .filter(s -> s.getQuantityOnHand() == 0)
                                .count();

                List<InventoryReportResponse.StockSummaryDto> stockSummary = allStocks.stream()
                                .map(s -> InventoryReportResponse.StockSummaryDto.builder()
                                                .productId(s.getProduct().getId())
                                                .productName(s.getProduct().getName())
                                                .productSku(s.getProduct().getSku())
                                                .warehouseName(s.getWarehouse().getName())
                                                .quantityOnHand(s.getQuantityOnHand())
                                                .minimumStockLevel(s.getProduct().getMinimumStockLevel())
                                                .isLowStock(s.getQuantityOnHand() <= s.getProduct()
                                                                .getMinimumStockLevel())
                                                .build())
                                .collect(Collectors.toList());

                // Recent movements
                List<InventoryReportResponse.StockMovementSummaryDto> recentMovements = stockMovementRepository
                                .findAll()
                                .stream()
                                .sorted(Comparator.comparing(
                                                com.brickerp.inventory.entity.StockMovement::getCreatedAt,
                                                Comparator.nullsLast(Comparator.reverseOrder())))
                                .limit(10)
                                .map(m -> InventoryReportResponse.StockMovementSummaryDto.builder()
                                                .productName(m.getProduct().getName())
                                                .movementType(m.getMovementType().name())
                                                .quantity(m.getQuantity())
                                                .warehouseName(m.getWarehouse().getName())
                                                .createdAt(m.getCreatedAt() != null
                                                                ? m.getCreatedAt().toString()
                                                                : "")
                                                .build())
                                .collect(Collectors.toList());

                return InventoryReportResponse.builder()
                                .totalProducts(productRepository.count())
                                .lowStockCount(lowStockCount)
                                .outOfStockCount(outOfStockCount)
                                .stockSummary(stockSummary)
                                .recentMovements(recentMovements)
                                .build();
        }

        @Override
        public FinanceReportResponse getFinanceReport() {
                List<Invoice> allInvoices = invoiceRepository.findAll();

                BigDecimal totalInvoiced = allInvoices.stream()
                                .filter(i -> i.getStatus() != Invoice.InvoiceStatus.CANCELLED)
                                .map(Invoice::getTotalAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalCollected = allInvoices.stream()
                                .map(Invoice::getPaidAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalOutstanding = allInvoices.stream()
                                .filter(i -> i.getStatus() != Invoice.InvoiceStatus.CANCELLED
                                                && i.getStatus() != Invoice.InvoiceStatus.PAID)
                                .map(Invoice::getBalanceDue)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                long paidInvoices = allInvoices.stream()
                                .filter(i -> i.getStatus() == Invoice.InvoiceStatus.PAID).count();

                long unpaidInvoices = allInvoices.stream()
                                .filter(i -> i.getStatus() == Invoice.InvoiceStatus.SENT
                                                || i.getStatus() == Invoice.InvoiceStatus.PARTIALLY_PAID)
                                .count();

                long overdueInvoices = allInvoices.stream()
                                .filter(i -> i.getStatus() != Invoice.InvoiceStatus.PAID
                                                && i.getStatus() != Invoice.InvoiceStatus.CANCELLED
                                                && i.getDueDate().isBefore(LocalDate.now()))
                                .count();

                // Outstanding invoices list
                List<FinanceReportResponse.OutstandingInvoiceDto> outstandingList = allInvoices.stream()
                                .filter(i -> i.getStatus() != Invoice.InvoiceStatus.PAID
                                                && i.getStatus() != Invoice.InvoiceStatus.CANCELLED)
                                .map(i -> FinanceReportResponse.OutstandingInvoiceDto.builder()
                                                .invoiceNumber(i.getInvoiceNumber())
                                                .customerName(i.getCustomer().getName())
                                                .totalAmount(i.getTotalAmount())
                                                .balanceDue(i.getBalanceDue())
                                                .dueDate(i.getDueDate().toString())
                                                .status(i.getStatus().name())
                                                .build())
                                .collect(Collectors.toList());

                return FinanceReportResponse.builder()
                                .totalInvoiced(totalInvoiced)
                                .totalCollected(totalCollected)
                                .totalOutstanding(totalOutstanding)
                                .paidInvoices(paidInvoices)
                                .unpaidInvoices(unpaidInvoices)
                                .overdueInvoices(overdueInvoices)
                                .outstandingInvoices(outstandingList)
                                .build();
        }
}