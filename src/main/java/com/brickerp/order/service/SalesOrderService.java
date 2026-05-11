package com.brickerp.order.service;

import com.brickerp.order.dto.*;

import java.time.LocalDate;
import java.util.List;

public interface SalesOrderService {
    SalesOrderResponse createOrder(SalesOrderRequest request);

    SalesOrderResponse getById(Long id);

    SalesOrderResponse getByOrderNumber(String orderNumber);

    List<SalesOrderSummaryResponse> getAllOrders();

    List<SalesOrderSummaryResponse> getOrdersByCustomer(Long customerId);

    List<SalesOrderSummaryResponse> getOrdersByStatus(String status);

    List<SalesOrderSummaryResponse> getOrdersByDateRange(LocalDate startDate, LocalDate endDate);

    SalesOrderResponse updateStatus(Long id, OrderStatusUpdateRequest request);

    SalesOrderResponse confirmOrder(Long id);

    SalesOrderResponse cancelOrder(Long id, String reason);
}