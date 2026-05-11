package com.brickerp.quality.repository;

import com.brickerp.quality.entity.QualityTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QualityTestRepository extends JpaRepository<QualityTest, Long> {
    List<QualityTest> findAllByOrderByCreatedAtDesc();

    List<QualityTest> findByProductIdOrderByCreatedAtDesc(Long productId);

    List<QualityTest> findByProductionOrderIdOrderByCreatedAtDesc(Long productionOrderId);

    @Query("SELECT MAX(q.testNumber) FROM QualityTest q WHERE q.testNumber LIKE 'QT-%'")
    Optional<String> findLastTestNumber();
}