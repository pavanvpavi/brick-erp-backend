package com.brickerp.procurement.repository;

import com.brickerp.procurement.entity.SupplierPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SupplierPriceHistoryRepository extends JpaRepository<SupplierPriceHistory, Long> {
    List<SupplierPriceHistory> findBySupplierIdOrderByEffectiveDateDesc(Long supplierId);

    List<SupplierPriceHistory> findByProductIdOrderByEffectiveDateDesc(Long productId);

    List<SupplierPriceHistory> findBySupplierIdAndProductIdOrderByEffectiveDateDesc(
            Long supplierId, Long productId);
}