package com.brickerp.procurement.repository;

import com.brickerp.procurement.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    List<Supplier> findByIsActiveTrue();

    boolean existsByGstin(String gstin);

    Optional<Supplier> findBySupplierCode(String supplierCode);

    @Query("SELECT MAX(s.supplierCode) FROM Supplier s WHERE s.supplierCode LIKE 'SUP-%'")
    Optional<String> findLastSupplierCode();
}