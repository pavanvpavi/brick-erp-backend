package com.brickerp.transfer.repository;

import com.brickerp.transfer.entity.StockTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockTransferRepository extends JpaRepository<StockTransfer, Long> {
    List<StockTransfer> findAllByOrderByCreatedAtDesc();

    List<StockTransfer> findByProductIdOrderByCreatedAtDesc(Long productId);

    List<StockTransfer> findByFromWarehouseIdOrToWarehouseIdOrderByCreatedAtDesc(
            Long fromId, Long toId);

    @Query("SELECT MAX(t.transferNumber) FROM StockTransfer t WHERE t.transferNumber LIKE 'TRF-%'")
    Optional<String> findLastTransferNumber();
}