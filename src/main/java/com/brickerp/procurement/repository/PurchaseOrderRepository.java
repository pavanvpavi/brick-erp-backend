package com.brickerp.procurement.repository;

import com.brickerp.procurement.entity.PurchaseOrder;
import com.brickerp.procurement.entity.PurchaseOrder.PoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    Optional<PurchaseOrder> findByPoNumber(String poNumber);

    List<PurchaseOrder> findBySupplierIdOrderByCreatedAtDesc(Long supplierId);

    List<PurchaseOrder> findByStatusOrderByCreatedAtDesc(PoStatus status);

    List<PurchaseOrder> findAllByOrderByCreatedAtDesc();

    @Query("SELECT MAX(p.poNumber) FROM PurchaseOrder p WHERE p.poNumber LIKE 'PO-%'")
    Optional<String> findLastPoNumber();
}