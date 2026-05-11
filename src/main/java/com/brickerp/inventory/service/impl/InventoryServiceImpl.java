package com.brickerp.inventory.service.impl;

import com.brickerp.common.exception.BusinessException;
import com.brickerp.common.exception.ResourceNotFoundException;
import com.brickerp.inventory.dto.*;
import com.brickerp.inventory.entity.Stock;
import com.brickerp.inventory.entity.StockMovement;
import com.brickerp.inventory.entity.StockMovement.MovementType;
import com.brickerp.inventory.entity.Warehouse;
import com.brickerp.inventory.repository.StockMovementRepository;
import com.brickerp.inventory.repository.StockRepository;
import com.brickerp.inventory.repository.WarehouseRepository;
import com.brickerp.inventory.service.InventoryService;
import com.brickerp.product.entity.Product;
import com.brickerp.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final WarehouseRepository warehouseRepository;
    private final StockRepository stockRepository;
    private final StockMovementRepository movementRepository;
    private final ProductRepository productRepository;

    // ==================== WAREHOUSE ====================

    @Override
    public WarehouseResponse createWarehouse(WarehouseRequest request) {
        if (warehouseRepository.existsByCode(request.getCode())) {
            throw new BusinessException("Warehouse with code '" + request.getCode() + "' already exists");
        }
        if (warehouseRepository.existsByName(request.getName())) {
            throw new BusinessException("Warehouse with name '" + request.getName() + "' already exists");
        }

        // If this is set as default, unset previous default
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            warehouseRepository.findByIsDefaultTrue()
                    .ifPresent(w -> {
                        w.setIsDefault(false);
                        warehouseRepository.save(w);
                    });
        }

        Warehouse warehouse = Warehouse.builder()
                .name(request.getName())
                .code(request.getCode().toUpperCase())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .contactPerson(request.getContactPerson())
                .contactPhone(request.getContactPhone())
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .build();

        return toWarehouseResponse(warehouseRepository.save(warehouse));
    }

    @Override
    public WarehouseResponse updateWarehouse(Long id, WarehouseRequest request) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", id));

        warehouse.setName(request.getName());
        warehouse.setAddress(request.getAddress());
        warehouse.setCity(request.getCity());
        warehouse.setState(request.getState());
        warehouse.setPincode(request.getPincode());
        warehouse.setContactPerson(request.getContactPerson());
        warehouse.setContactPhone(request.getContactPhone());

        return toWarehouseResponse(warehouseRepository.save(warehouse));
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseResponse getWarehouseById(Long id) {
        return warehouseRepository.findById(id)
                .map(this::toWarehouseResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseResponse> getAllWarehouses() {
        return warehouseRepository.findByIsActiveTrue()
                .stream().map(this::toWarehouseResponse).collect(Collectors.toList());
    }

    @Override
    public void deleteWarehouse(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", id));
        warehouse.setIsActive(false);
        warehouseRepository.save(warehouse);
    }

    // ==================== STOCK ====================

    @Override
    @Transactional(readOnly = true)
    public StockResponse getStock(Long productId, Long warehouseId) {
        return stockRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .map(this::toStockResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Stock not found for product " + productId + " in warehouse " + warehouseId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockResponse> getStockByWarehouse(Long warehouseId) {
        return stockRepository.findByWarehouseId(warehouseId)
                .stream().map(this::toStockResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockResponse> getStockByProduct(Long productId) {
        return stockRepository.findByProductId(productId)
                .stream().map(this::toStockResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockResponse> getLowStockItems() {
        return stockRepository.findLowStockItems()
                .stream().map(this::toStockResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getTotalStock(Long productId) {
        return stockRepository.getTotalStockByProduct(productId);
    }

    // ==================== STOCK MOVEMENTS ====================

    @Override
    public StockMovementResponse adjustStock(StockAdjustmentRequest request) {
        MovementType type;
        try {
            type = MovementType.valueOf(request.getAdjustmentType());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid adjustment type: " + request.getAdjustmentType()
                    + ". Use ADJUSTMENT_IN or ADJUSTMENT_OUT");
        }

        if (type == MovementType.ADJUSTMENT_IN) {
            addStock(request.getProductId(), request.getWarehouseId(),
                    request.getQuantity(), "MANUAL", null, request.getNotes());
        } else if (type == MovementType.ADJUSTMENT_OUT) {
            deductStock(request.getProductId(), request.getWarehouseId(),
                    request.getQuantity(), "MANUAL", null, request.getNotes());
        } else {
            throw new BusinessException("Adjustment type must be ADJUSTMENT_IN or ADJUSTMENT_OUT");
        }

        // Return last movement record
        return movementRepository
                .findByProductIdAndWarehouseIdOrderByCreatedAtDesc(
                        request.getProductId(), request.getWarehouseId())
                .stream().findFirst()
                .map(this::toMovementResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Movement record not found"));
    }

    @Override
    public void addStock(Long productId, Long warehouseId, Integer quantity,
            String referenceType, Long referenceId, String notes) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", warehouseId));

        Stock stock = stockRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseGet(() -> Stock.builder()
                        .product(product)
                        .warehouse(warehouse)
                        .quantityOnHand(0)
                        .quantityReserved(0)
                        .build());

        int quantityBefore = stock.getQuantityOnHand();
        stock.setQuantityOnHand(quantityBefore + quantity);
        stockRepository.save(stock);

        // Determine movement type
        MovementType movementType = "MANUAL".equals(referenceType)
                ? MovementType.ADJUSTMENT_IN
                : MovementType.STOCK_IN;

        recordMovement(product, warehouse, movementType, quantity,
                quantityBefore, stock.getQuantityOnHand(), referenceType, referenceId, notes);
    }

    @Override
    public void deductStock(Long productId, Long warehouseId, Integer quantity,
            String referenceType, Long referenceId, String notes) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", warehouseId));

        Stock stock = stockRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new BusinessException(
                        "No stock found for product in this warehouse"));

        if (stock.getAvailableQuantity() < quantity) {
            throw new BusinessException("Insufficient stock. Available: "
                    + stock.getAvailableQuantity() + ", Requested: " + quantity);
        }

        int quantityBefore = stock.getQuantityOnHand();
        stock.setQuantityOnHand(quantityBefore - quantity);
        stockRepository.save(stock);

        MovementType movementType = "MANUAL".equals(referenceType)
                ? MovementType.ADJUSTMENT_OUT
                : MovementType.STOCK_OUT;

        recordMovement(product, warehouse, movementType, quantity,
                quantityBefore, stock.getQuantityOnHand(), referenceType, referenceId, notes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementResponse> getMovementsByProduct(Long productId) {
        return movementRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream().map(this::toMovementResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementResponse> getMovementsByWarehouse(Long warehouseId) {
        return movementRepository.findByWarehouseIdOrderByCreatedAtDesc(warehouseId)
                .stream().map(this::toMovementResponse).collect(Collectors.toList());
    }

    // ==================== PRIVATE HELPERS ====================

    private void recordMovement(Product product, Warehouse warehouse, MovementType type,
            Integer quantity, Integer before, Integer after,
            String refType, Long refId, String notes) {
        StockMovement movement = StockMovement.builder()
                .product(product)
                .warehouse(warehouse)
                .movementType(type)
                .quantity(quantity)
                .quantityBefore(before)
                .quantityAfter(after)
                .referenceType(refType)
                .referenceId(refId)
                .notes(notes)
                .build();
        movementRepository.save(movement);
    }

    private WarehouseResponse toWarehouseResponse(Warehouse w) {
        WarehouseResponse r = new WarehouseResponse();
        r.setId(w.getId());
        r.setName(w.getName());
        r.setCode(w.getCode());
        r.setAddress(w.getAddress());
        r.setCity(w.getCity());
        r.setState(w.getState());
        r.setPincode(w.getPincode());
        r.setContactPerson(w.getContactPerson());
        r.setContactPhone(w.getContactPhone());
        r.setIsDefault(w.getIsDefault());
        r.setIsActive(w.getIsActive());
        r.setCreatedAt(w.getCreatedAt());
        return r;
    }

    private StockResponse toStockResponse(Stock s) {
        StockResponse r = new StockResponse();
        r.setId(s.getId());
        r.setProductId(s.getProduct().getId());
        r.setProductName(s.getProduct().getName());
        r.setProductSku(s.getProduct().getSku());
        r.setWarehouseId(s.getWarehouse().getId());
        r.setWarehouseName(s.getWarehouse().getName());
        r.setWarehouseCode(s.getWarehouse().getCode());
        r.setQuantityOnHand(s.getQuantityOnHand());
        r.setQuantityReserved(s.getQuantityReserved());
        r.setAvailableQuantity(s.getAvailableQuantity());
        r.setMinimumStockLevel(s.getProduct().getMinimumStockLevel());
        r.setIsLowStock(s.getQuantityOnHand() <= s.getProduct().getMinimumStockLevel());
        r.setUpdatedAt(s.getUpdatedAt());
        return r;
    }

    private StockMovementResponse toMovementResponse(StockMovement m) {
        StockMovementResponse r = new StockMovementResponse();
        r.setId(m.getId());
        r.setProductId(m.getProduct().getId());
        r.setProductName(m.getProduct().getName());
        r.setProductSku(m.getProduct().getSku());
        r.setWarehouseId(m.getWarehouse().getId());
        r.setWarehouseName(m.getWarehouse().getName());
        r.setMovementType(m.getMovementType().name());
        r.setQuantity(m.getQuantity());
        r.setQuantityBefore(m.getQuantityBefore());
        r.setQuantityAfter(m.getQuantityAfter());
        r.setReferenceType(m.getReferenceType());
        r.setReferenceId(m.getReferenceId());
        r.setNotes(m.getNotes());
        r.setCreatedAt(m.getCreatedAt());
        return r;
    }
}