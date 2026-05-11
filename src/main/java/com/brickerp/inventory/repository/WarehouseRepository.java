package com.brickerp.inventory.repository;

import com.brickerp.inventory.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    List<Warehouse> findByIsActiveTrue();

    Optional<Warehouse> findByCode(String code);

    Optional<Warehouse> findByIsDefaultTrue();

    boolean existsByCode(String code);

    boolean existsByName(String name);
}