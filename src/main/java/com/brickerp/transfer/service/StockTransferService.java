package com.brickerp.transfer.service;

import com.brickerp.transfer.dto.StockTransferRequest;
import com.brickerp.transfer.dto.StockTransferResponse;
import java.util.List;

public interface StockTransferService {
    StockTransferResponse transfer(StockTransferRequest request);

    StockTransferResponse cancelTransfer(Long id, String reason);

    List<StockTransferResponse> getAll();

    List<StockTransferResponse> getByProduct(Long productId);

    List<StockTransferResponse> getByWarehouse(Long warehouseId);

    StockTransferResponse getById(Long id);
}