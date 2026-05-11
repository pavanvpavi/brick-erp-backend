package com.brickerp.order.repository;

import com.brickerp.order.entity.SalesOrder;
import com.brickerp.order.entity.SalesOrder.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    Optional<SalesOrder> findByOrderNumber(String orderNumber);

    List<SalesOrder> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<SalesOrder> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    List<SalesOrder> findAllByOrderByCreatedAtDesc();

    boolean existsByOrderNumber(String orderNumber);

    @Query("SELECT MAX(s.orderNumber) FROM SalesOrder s WHERE s.orderNumber LIKE 'SO-%'")
    Optional<String> findLastOrderNumber();

    @Query("SELECT s FROM SalesOrder s WHERE s.orderDate BETWEEN :startDate AND :endDate ORDER BY s.orderDate DESC")
    List<SalesOrder> findByDateRange(LocalDate startDate, LocalDate endDate);
}