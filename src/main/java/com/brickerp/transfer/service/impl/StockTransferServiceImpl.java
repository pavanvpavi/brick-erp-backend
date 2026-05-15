package com.brickerp.transfer.service.impl;

import com.brickerp.common.exception.BusinessException;
import com.brickerp.common.exception.ResourceNotFoundException;
import com.brickerp.inventory.repository.StockRepository;
import com.brickerp.inventory.repository.WarehouseRepository;
import com.brickerp.inventory.service.InventoryService;
import com.brickerp.product.entity.Product;
import com.brickerp.product.repository.ProductRepository;
import com.brickerp.transfer.dto.StockTransferRequest;
import com.brickerp.transfer.dto.StockTransferResponse;
import com.brickerp.transfer.entity.StockTransfer;
import com.brickerp.transfer.entity.StockTransfer.TransferStatus;
import com.brickerp.transfer.repository.StockTransferRepository;
import com.brickerp.transfer.service.StockTransferService;
import com.brickerp.inventory.entity.Warehouse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StockTransferServiceImpl implements StockTransferService {

    private final StockTransferRepository transferRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockRepository stockRepository;
    private final InventoryService inventoryService;

    @Override
    public StockTransferResponse transfer(StockTransferRequest request) {
        if (request.getFromWarehouseId().equals(request.getToWarehouseId())) {
            throw new BusinessException("Source and destination warehouses cannot be the same");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));

        Warehouse fromWarehouse = warehouseRepository.findById(request.getFromWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", request.getFromWarehouseId()));

        Warehouse toWarehouse = warehouseRepository.findById(request.getToWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", request.getToWarehouseId()));

        // Check available stock in source warehouse
        Integer available = stockRepository
                .findByProductIdAndWarehouseId(request.getProductId(), request.getFromWarehouseId())
                .map(s -> s.getAvailableQuantity())
                .orElse(0);

        if (available < request.getQuantity()) {
            throw new BusinessException("Insufficient stock in source warehouse. Available: "
                    + available + ", Requested: " + request.getQuantity());
        }

        // Deduct from source warehouse
        inventoryService.deductStock(
                request.getProductId(),
                request.getFromWarehouseId(),
                request.getQuantity(),
                "STOCK_TRANSFER",
                null,
                "Transfer out to: " + toWarehouse.getName());

        // Add to destination warehouse
        inventoryService.addStock(
                request.getProductId(),
                request.getToWarehouseId(),
                request.getQuantity(),
                "STOCK_TRANSFER",
                null,
                "Transfer in from: " + fromWarehouse.getName());

        StockTransfer transfer = StockTransfer.builder()
                .transferNumber(generateTransferNumber())
                .product(product)
                .fromWarehouse(fromWarehouse)
                .toWarehouse(toWarehouse)
                .quantity(request.getQuantity())
                .status(TransferStatus.COMPLETED)
                .notes(request.getNotes())
                .build();

        return toResponse(transferRepository.save(transfer));
    }

    @Override
    public StockTransferResponse cancelTransfer(Long id, String reason) {
        StockTransfer transfer = transferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockTransfer", id));

        if (transfer.getStatus() == TransferStatus.CANCELLED) {
            throw new BusinessException("Transfer is already cancelled");
        }
        if (transfer.getStatus() == TransferStatus.COMPLETED) {
            throw new BusinessException("Completed transfers cannot be cancelled. Please do a reverse transfer.");
        }

        transfer.setStatus(TransferStatus.CANCELLED);
        transfer.setNotes(reason != null ? reason : transfer.getNotes());
        return toResponse(transferRepository.save(transfer));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockTransferResponse> getAll() {
        return transferRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockTransferResponse> getByProduct(Long productId) {
        return transferRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockTransferResponse> getByWarehouse(Long warehouseId) {
        return transferRepository
                .findByFromWarehouseIdOrToWarehouseIdOrderByCreatedAtDesc(warehouseId, warehouseId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StockTransferResponse getById(Long id) {
        return transferRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("StockTransfer", id));
    }

    private String generateTransferNumber() {
        return transferRepository.findLastTransferNumber()
                .map(last -> {
                    int num = Integer.parseInt(last.replace("TRF-", ""));
                    return String.format("TRF-%05d", num + 1);
                })
                .orElse("TRF-00001");
    }

    private StockTransferResponse toResponse(StockTransfer t) {
        StockTransferResponse r = new StockTransferResponse();
        r.setId(t.getId());
        r.setTransferNumber(t.getTransferNumber());
        r.setProductId(t.getProduct().getId());
        r.setProductName(t.getProduct().getName());
        r.setProductSku(t.getProduct().getSku());
        r.setFromWarehouseId(t.getFromWarehouse().getId());
        r.setFromWarehouseName(t.getFromWarehouse().getName());
        r.setToWarehouseId(t.getToWarehouse().getId());
        r.setToWarehouseName(t.getToWarehouse().getName());
        r.setQuantity(t.getQuantity());
        r.setStatus(t.getStatus().name());
        r.setNotes(t.getNotes());
        r.setCreatedAt(t.getCreatedAt());
        return r;
    }
}