package com.brickerp.finance.repository;

import com.brickerp.finance.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByInvoiceIdOrderByPaymentDateDesc(Long invoiceId);

    @Query("SELECT MAX(p.paymentNumber) FROM Payment p WHERE p.paymentNumber LIKE 'PAY-%'")
    Optional<String> findLastPaymentNumber();
}