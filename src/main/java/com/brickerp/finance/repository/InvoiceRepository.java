package com.brickerp.finance.repository;

import com.brickerp.finance.entity.Invoice;
import com.brickerp.finance.entity.Invoice.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    Optional<Invoice> findBySalesOrderId(Long salesOrderId);

    List<Invoice> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Invoice> findByStatusOrderByCreatedAtDesc(InvoiceStatus status);

    List<Invoice> findAllByOrderByCreatedAtDesc();

    @Query("SELECT MAX(i.invoiceNumber) FROM Invoice i WHERE i.invoiceNumber LIKE 'INV-%'")
    Optional<String> findLastInvoiceNumber();
}