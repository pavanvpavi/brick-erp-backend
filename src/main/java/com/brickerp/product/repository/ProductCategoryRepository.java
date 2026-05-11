package com.brickerp.product.repository;

import com.brickerp.product.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    Optional<ProductCategory> findByName(String name);

    Optional<ProductCategory> findByCode(String code);

    List<ProductCategory> findByIsActiveTrue();

    boolean existsByName(String name);

    boolean existsByCode(String code);
}