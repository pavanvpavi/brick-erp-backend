package com.brickerp.product.repository;

import com.brickerp.product.entity.UnitOfMeasure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UnitOfMeasureRepository extends JpaRepository<UnitOfMeasure, Long> {
    Optional<UnitOfMeasure> findByAbbreviation(String abbreviation);

    List<UnitOfMeasure> findByIsActiveTrue();

    boolean existsByAbbreviation(String abbreviation);

    boolean existsByName(String name);
}