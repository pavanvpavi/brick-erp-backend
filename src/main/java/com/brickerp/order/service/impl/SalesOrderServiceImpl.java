package com.brickerp.order.service.impl;

import com.brickerp.common.exception.BusinessException;
import com.brickerp.common.exception.ResourceNotFoundException;
import com.brickerp.customer.entity.Customer;
import com.brickerp.customer.entity.CustomerAddress;
import com.brickerp.customer.repository.CustomerAddressRepository;
import com.brickerp.customer.repository.CustomerRepository;
import com.brickerp.inventory.entity.Warehouse;
import com.brickerp.inventory.repository.StockRepository;
import com.brickerp.inventory.repository.WarehouseRepository;
import com.brickerp.inventory.service.InventoryService;
import com.brickerp.order.dto.*;
import com.brickerp.order.entity.SalesOrder;
import com.brickerp.order.entity.SalesOrder.OrderStatus;
import com.brickerp.order.entity.SalesOrderItem;
import com.brickerp.order.repository.SalesOrderRepository;
import com.brickerp.order.service.SalesOrderService;
import com.brickerp.product.entity.Product;
import com.brickerp.product.repository.ProductRepository;
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
@Transactional
public class SalesOrderServiceImpl implements SalesOrderService {

    private final SalesOrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockRepository stockRepository;
    private final InventoryService inventoryService;

    @Override
    public SalesOrderResponse createOrder(SalesOrderRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.getCustomerId()));

        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", request.getWarehouseId()));

        CustomerAddress deliveryAddress = null;
        if (request.getDeliveryAddressId() != null) {
            deliveryAddress = addressRepository.findById(request.getDeliveryAddressId())
                    .orElseThrow(() -> new ResourceNotFoundException("CustomerAddress",
                            request.getDeliveryAddressId()));
        }

        // Validate stock availability for all items first
        for (SalesOrderItemRequest itemReq : request.getItems()) {
            Integer available = stockRepository
                    .findByProductIdAndWarehouseId(itemReq.getProductId(), request.getWarehouseId())
                    .map(s -> s.getAvailableQuantity())
                    .orElse(0);
            if (available < itemReq.getQuantity()) {
                Product p = productRepository.findById(itemReq.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product", itemReq.getProductId()));
                throw new BusinessException("Insufficient stock for product '" + p.getName()
                        + "'. Available: " + available + ", Requested: " + itemReq.getQuantity());
            }
        }

        SalesOrder order = SalesOrder.builder()
                .orderNumber(generateOrderNumber())
                .customer(customer)
                .deliveryAddress(deliveryAddress)
                .warehouseId(request.getWarehouseId())
                .orderDate(LocalDate.now())
                .expectedDeliveryDate(request.getExpectedDeliveryDate())
                .status(OrderStatus.DRAFT)
                .notes(request.getNotes())
                .build();

        // Build order items and calculate totals
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;

        for (SalesOrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", itemReq.getProductId()));

            BigDecimal unitPrice = itemReq.getUnitPrice() != null
                    ? itemReq.getUnitPrice()
                    : product.getSellingPrice();

            BigDecimal discountPct = itemReq.getDiscountPercentage() != null
                    ? itemReq.getDiscountPercentage()
                    : BigDecimal.ZERO;

            BigDecimal baseAmount = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            BigDecimal discountAmt = baseAmount.multiply(discountPct)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal taxableAmount = baseAmount.subtract(discountAmt);
            BigDecimal taxAmt = taxableAmount.multiply(product.getTaxPercentage())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal lineTotal = taxableAmount.add(taxAmt);

            SalesOrderItem item = SalesOrderItem.builder()
                    .salesOrder(order)
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(unitPrice)
                    .taxPercentage(product.getTaxPercentage())
                    .taxAmount(taxAmt)
                    .discountPercentage(discountPct)
                    .discountAmount(discountAmt)
                    .lineTotal(lineTotal)
                    .notes(itemReq.getNotes())
                    .build();

            order.getItems().add(item);
            subtotal = subtotal.add(baseAmount);
            totalTax = totalTax.add(taxAmt);
            totalDiscount = totalDiscount.add(discountAmt);
        }

        order.setSubtotal(subtotal);
        order.setTaxAmount(totalTax);
        order.setDiscountAmount(totalDiscount);
        order.setTotalAmount(subtotal.subtract(totalDiscount).add(totalTax));

        return toResponse(orderRepository.save(order));
    }

    @Override
    public SalesOrderResponse confirmOrder(Long id) {
        SalesOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", id));

        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new BusinessException("Only DRAFT orders can be confirmed. Current status: "
                    + order.getStatus());
        }

        // Deduct stock for each item
        for (SalesOrderItem item : order.getItems()) {
            inventoryService.deductStock(
                    item.getProduct().getId(),
                    order.getWarehouseId(),
                    item.getQuantity(),
                    "SALES_ORDER",
                    order.getId(),
                    "Order confirmed: " + order.getOrderNumber());
        }

        order.setStatus(OrderStatus.CONFIRMED);
        return toResponse(orderRepository.save(order));
    }

    @Override
    public SalesOrderResponse updateStatus(Long id, OrderStatusUpdateRequest request) {
        SalesOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", id));

        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(request.getStatus());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid status: " + request.getStatus());
        }

        // Prevent going backward in status
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException("Cannot update a cancelled order");
        }
        if (newStatus == OrderStatus.DELIVERED) {
            order.setDeliveryDate(LocalDate.now());
        }

        order.setStatus(newStatus);
        if (request.getNotes() != null) {
            order.setNotes(request.getNotes());
        }

        return toResponse(orderRepository.save(order));
    }

    @Override
    public SalesOrderResponse cancelOrder(Long id, String reason) {
        SalesOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", id));

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new BusinessException("Cannot cancel a delivered order");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException("Order is already cancelled");
        }

        // If confirmed or beyond, restore stock
        if (order.getStatus() != OrderStatus.DRAFT) {
            for (SalesOrderItem item : order.getItems()) {
                inventoryService.addStock(
                        item.getProduct().getId(),
                        order.getWarehouseId(),
                        item.getQuantity(),
                        "SALES_ORDER_CANCEL",
                        order.getId(),
                        "Order cancelled: " + order.getOrderNumber());
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setNotes(reason != null ? reason : order.getNotes());
        return toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public SalesOrderResponse getById(Long id) {
        return orderRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", id));
    }

    @Override
    @Transactional(readOnly = true)
    public SalesOrderResponse getByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "SalesOrder not found with number: " + orderNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesOrderSummaryResponse> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toSummaryResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesOrderSummaryResponse> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream().map(this::toSummaryResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesOrderSummaryResponse> getOrdersByStatus(String status) {
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            return orderRepository.findByStatusOrderByCreatedAtDesc(orderStatus)
                    .stream().map(this::toSummaryResponse).collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid status: " + status);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesOrderSummaryResponse> getOrdersByDateRange(
            LocalDate startDate, LocalDate endDate) {
        return orderRepository.findByDateRange(startDate, endDate)
                .stream().map(this::toSummaryResponse).collect(Collectors.toList());
    }

    // ==================== PRIVATE HELPERS ====================

    private String generateOrderNumber() {
        return orderRepository.findLastOrderNumber()
                .map(last -> {
                    int num = Integer.parseInt(last.replace("SO-", ""));
                    return String.format("SO-%05d", num + 1);
                })
                .orElse("SO-00001");
    }

    private SalesOrderResponse toResponse(SalesOrder o) {
        SalesOrderResponse r = new SalesOrderResponse();
        r.setId(o.getId());
        r.setOrderNumber(o.getOrderNumber());
        r.setCustomerId(o.getCustomer().getId());
        r.setCustomerName(o.getCustomer().getName());
        r.setCustomerCode(o.getCustomer().getCustomerCode());
        r.setStatus(o.getStatus().name());
        r.setOrderDate(o.getOrderDate());
        r.setExpectedDeliveryDate(o.getExpectedDeliveryDate());
        r.setDeliveryDate(o.getDeliveryDate());
        r.setSubtotal(o.getSubtotal());
        r.setTaxAmount(o.getTaxAmount());
        r.setDiscountAmount(o.getDiscountAmount());
        r.setTotalAmount(o.getTotalAmount());
        r.setWarehouseId(o.getWarehouseId());
        r.setNotes(o.getNotes());
        r.setCreatedAt(o.getCreatedAt());
        r.setUpdatedAt(o.getUpdatedAt());
        r.setItems(o.getItems().stream().map(this::toItemResponse).collect(Collectors.toList()));

        // Set warehouse name
        warehouseRepository.findById(o.getWarehouseId())
                .ifPresent(w -> r.setWarehouseName(w.getName()));

        return r;
    }

    private SalesOrderSummaryResponse toSummaryResponse(SalesOrder o) {
        SalesOrderSummaryResponse r = new SalesOrderSummaryResponse();
        r.setId(o.getId());
        r.setOrderNumber(o.getOrderNumber());
        r.setCustomerName(o.getCustomer().getName());
        r.setCustomerCode(o.getCustomer().getCustomerCode());
        r.setStatus(o.getStatus().name());
        r.setOrderDate(o.getOrderDate());
        r.setTotalAmount(o.getTotalAmount());
        r.setItemCount(o.getItems().size());
        r.setCreatedAt(o.getCreatedAt());
        return r;
    }

    private SalesOrderItemResponse toItemResponse(SalesOrderItem i) {
        SalesOrderItemResponse r = new SalesOrderItemResponse();
        r.setId(i.getId());
        r.setProductId(i.getProduct().getId());
        r.setProductName(i.getProduct().getName());
        r.setProductSku(i.getProduct().getSku());
        r.setUomAbbreviation(i.getProduct().getUnitOfMeasure().getAbbreviation());
        r.setQuantity(i.getQuantity());
        r.setUnitPrice(i.getUnitPrice());
        r.setTaxPercentage(i.getTaxPercentage());
        r.setTaxAmount(i.getTaxAmount());
        r.setDiscountPercentage(i.getDiscountPercentage());
        r.setDiscountAmount(i.getDiscountAmount());
        r.setLineTotal(i.getLineTotal());
        r.setNotes(i.getNotes());
        return r;
    }
}