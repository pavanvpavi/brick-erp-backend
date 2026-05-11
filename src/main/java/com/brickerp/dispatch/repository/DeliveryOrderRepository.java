package com.brickerp.dispatch.repository;

import com.brickerp.dispatch.entity.DeliveryOrder;
import com.brickerp.dispatch.entity.DeliveryOrder.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryOrderRepository extends JpaRepository<DeliveryOrder, Long> {
    List<DeliveryOrder> findAllByOrderByCreatedAtDesc();

    List<DeliveryOrder> findByStatusOrderByCreatedAtDesc(DeliveryStatus status);

    Optional<DeliveryOrder> findBySalesOrderId(Long salesOrderId);

    @Query("SELECT MAX(d.deliveryNumber) FROM DeliveryOrder d WHERE d.deliveryNumber LIKE 'DEL-%'")
    Optional<String> findLastDeliveryNumber();
}