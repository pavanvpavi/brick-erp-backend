package com.brickerp.manufacturing.service.impl;

import com.brickerp.common.exception.BusinessException;
import com.brickerp.common.exception.ResourceNotFoundException;
import com.brickerp.inventory.repository.StockRepository;
import com.brickerp.inventory.repository.WarehouseRepository;
import com.brickerp.inventory.service.InventoryService;
import com.brickerp.manufacturing.dto.*;
import com.brickerp.manufacturing.entity.*;
import com.brickerp.manufacturing.entity.ProductionOrder.ProductionStatus;
import com.brickerp.manufacturing.repository.*;
import com.brickerp.manufacturing.service.ManufacturingService;
import com.brickerp.product.entity.Product;
import com.brickerp.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ManufacturingServiceImpl implements ManufacturingService {

    private final BomRepository bomRepository;
    private final ProductionOrderRepository productionOrderRepository;
    private final ProductionConsumptionRepository consumptionRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockRepository stockRepository;
    private final InventoryService inventoryService;

    // ==================== BOM ====================

    @Override
    public BomResponse createBom(BomRequest request) {
        Product finishedProduct = productRepository.findById(request.getFinishedProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product",
                        request.getFinishedProductId()));

        // If this is default, unset previous default BOM for this product
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            bomRepository.findByFinishedProductIdAndIsDefaultTrue(finishedProduct.getId())
                    .ifPresent(b -> {
                        b.setIsDefault(false);
                        bomRepository.save(b);
                    });
        }

        BillOfMaterials bom = BillOfMaterials.builder()
                .finishedProduct(finishedProduct)
                .name(request.getName())
                .version(request.getVersion())
                .outputQuantity(request.getOutputQuantity())
                .description(request.getDescription())
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .build();

        for (BomItemRequest itemReq : request.getBomItems()) {
            Product material = productRepository.findById(itemReq.getMaterialProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product",
                            itemReq.getMaterialProductId()));

            BomItem item = BomItem.builder()
                    .billOfMaterials(bom)
                    .materialProduct(material)
                    .quantityRequired(itemReq.getQuantityRequired())
                    .notes(itemReq.getNotes())
                    .build();
            bom.getBomItems().add(item);
        }

        return toBomResponse(bomRepository.save(bom));
    }

    @Override
    @Transactional(readOnly = true)
    public BomResponse getBomById(Long id) {
        return bomRepository.findById(id)
                .map(this::toBomResponse)
                .orElseThrow(() -> new ResourceNotFoundException("BOM", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BomResponse> getAllBoms() {
        return bomRepository.findByIsActiveTrue()
                .stream().map(this::toBomResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BomResponse> getBomsByProduct(Long productId) {
        return bomRepository.findByFinishedProductId(productId)
                .stream().map(this::toBomResponse).collect(Collectors.toList());
    }

    @Override
    public void deleteBom(Long id) {
        BillOfMaterials bom = bomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BOM", id));
        bom.setIsActive(false);
        bomRepository.save(bom);
    }

    // ==================== PRODUCTION ORDERS ====================

    @Override
    public ProductionOrderResponse createProductionOrder(ProductionOrderRequest request) {
        BillOfMaterials bom = bomRepository.findById(request.getBomId())
                .orElseThrow(() -> new ResourceNotFoundException("BOM", request.getBomId()));

        warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse",
                        request.getWarehouseId()));

        // Calculate how many BOM runs needed
        int bomRuns = (int) Math.ceil(
                (double) request.getPlannedQuantity() / bom.getOutputQuantity());

        // Check raw material availability
        for (BomItem bomItem : bom.getBomItems()) {
            double totalRequired = bomItem.getQuantityRequired() * bomRuns;
            Integer available = stockRepository
                    .findByProductIdAndWarehouseId(
                            bomItem.getMaterialProduct().getId(), request.getWarehouseId())
                    .map(s -> s.getAvailableQuantity())
                    .orElse(0);

            if (available < totalRequired) {
                throw new BusinessException("Insufficient stock for material '"
                        + bomItem.getMaterialProduct().getName()
                        + "'. Required: " + totalRequired
                        + ", Available: " + available);
            }
        }

        ProductionOrder order = ProductionOrder.builder()
                .productionNumber(generateProductionNumber())
                .finishedProduct(bom.getFinishedProduct())
                .billOfMaterials(bom)
                .plannedQuantity(request.getPlannedQuantity())
                .warehouseId(request.getWarehouseId())
                .plannedStartDate(request.getPlannedStartDate())
                .plannedEndDate(request.getPlannedEndDate())
                .notes(request.getNotes())
                .status(ProductionStatus.PLANNED)
                .build();

        // Create consumption plan
        for (BomItem bomItem : bom.getBomItems()) {
            double totalRequired = bomItem.getQuantityRequired() * bomRuns;
            ProductionConsumption consumption = ProductionConsumption.builder()
                    .productionOrder(order)
                    .materialProduct(bomItem.getMaterialProduct())
                    .plannedQuantity(totalRequired)
                    .consumedQuantity(0.0)
                    .warehouseId(request.getWarehouseId())
                    .build();
            order.getConsumptions().add(consumption);
        }

        return toProductionOrderResponse(productionOrderRepository.save(order));
    }

    @Override
    public ProductionOrderResponse startProduction(Long id) {
        ProductionOrder order = productionOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductionOrder", id));

        if (order.getStatus() != ProductionStatus.PLANNED) {
            throw new BusinessException("Only PLANNED production orders can be started");
        }

        // Deduct raw materials from stock
        for (ProductionConsumption consumption : order.getConsumptions()) {
            inventoryService.deductStock(
                    consumption.getMaterialProduct().getId(),
                    consumption.getWarehouseId(),
                    consumption.getPlannedQuantity().intValue(),
                    "PRODUCTION_ORDER",
                    order.getId(),
                    "Material consumed for: " + order.getProductionNumber());
            consumption.setConsumedQuantity(consumption.getPlannedQuantity());
            consumptionRepository.save(consumption);
        }

        order.setStatus(ProductionStatus.IN_PROGRESS);
        order.setActualStartDate(LocalDate.now());
        return toProductionOrderResponse(productionOrderRepository.save(order));
    }

    @Override
    public ProductionOrderResponse completeProduction(Long id,
            CompleteProductionRequest request) {
        ProductionOrder order = productionOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductionOrder", id));

        if (order.getStatus() != ProductionStatus.IN_PROGRESS) {
            throw new BusinessException("Only IN_PROGRESS production orders can be completed");
        }

        // Add finished goods to stock
        inventoryService.addStock(
                order.getFinishedProduct().getId(),
                order.getWarehouseId(),
                request.getProducedQuantity(),
                "PRODUCTION_ORDER",
                order.getId(),
                "Production completed: " + order.getProductionNumber());

        order.setProducedQuantity(request.getProducedQuantity());
        order.setStatus(ProductionStatus.COMPLETED);
        order.setActualEndDate(LocalDate.now());
        if (request.getNotes() != null)
            order.setNotes(request.getNotes());

        return toProductionOrderResponse(productionOrderRepository.save(order));
    }

    @Override
    public ProductionOrderResponse cancelProductionOrder(Long id, String reason) {
        ProductionOrder order = productionOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductionOrder", id));

        if (order.getStatus() == ProductionStatus.COMPLETED) {
            throw new BusinessException("Cannot cancel a completed production order");
        }
        if (order.getStatus() == ProductionStatus.CANCELLED) {
            throw new BusinessException("Production order is already cancelled");
        }

        // If in progress, return consumed materials back to stock
        if (order.getStatus() == ProductionStatus.IN_PROGRESS) {
            for (ProductionConsumption consumption : order.getConsumptions()) {
                if (consumption.getConsumedQuantity() > 0) {
                    inventoryService.addStock(
                            consumption.getMaterialProduct().getId(),
                            consumption.getWarehouseId(),
                            consumption.getConsumedQuantity().intValue(),
                            "PRODUCTION_CANCEL",
                            order.getId(),
                            "Production cancelled: " + order.getProductionNumber());
                }
            }
        }

        order.setStatus(ProductionStatus.CANCELLED);
        order.setNotes(reason != null ? reason : order.getNotes());
        return toProductionOrderResponse(productionOrderRepository.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductionOrderResponse getProductionOrderById(Long id) {
        return productionOrderRepository.findById(id)
                .map(this::toProductionOrderResponse)
                .orElseThrow(() -> new ResourceNotFoundException("ProductionOrder", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductionOrderResponse> getAllProductionOrders() {
        return productionOrderRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toProductionOrderResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductionOrderResponse> getProductionOrdersByStatus(String status) {
        try {
            ProductionStatus ps = ProductionStatus.valueOf(status.toUpperCase());
            return productionOrderRepository.findByStatusOrderByCreatedAtDesc(ps)
                    .stream().map(this::toProductionOrderResponse).collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid status: " + status);
        }
    }

    // ==================== PRIVATE HELPERS ====================

    private String generateProductionNumber() {
        return productionOrderRepository.findLastProductionNumber()
                .map(last -> {
                    int num = Integer.parseInt(last.replace("PRD-", ""));
                    return String.format("PRD-%05d", num + 1);
                })
                .orElse("PRD-00001");
    }

    private BomResponse toBomResponse(BillOfMaterials b) {
        BomResponse r = new BomResponse();
        r.setId(b.getId());
        r.setFinishedProductId(b.getFinishedProduct().getId());
        r.setFinishedProductName(b.getFinishedProduct().getName());
        r.setFinishedProductSku(b.getFinishedProduct().getSku());
        r.setName(b.getName());
        r.setVersion(b.getVersion());
        r.setOutputQuantity(b.getOutputQuantity());
        r.setDescription(b.getDescription());
        r.setIsDefault(b.getIsDefault());
        r.setIsActive(b.getIsActive());
        r.setCreatedAt(b.getCreatedAt());
        r.setBomItems(b.getBomItems().stream()
                .map(this::toBomItemResponse).collect(Collectors.toList()));
        return r;
    }

    private BomItemResponse toBomItemResponse(BomItem i) {
        BomItemResponse r = new BomItemResponse();
        r.setId(i.getId());
        r.setMaterialProductId(i.getMaterialProduct().getId());
        r.setMaterialProductName(i.getMaterialProduct().getName());
        r.setMaterialProductSku(i.getMaterialProduct().getSku());
        r.setUomAbbreviation(i.getMaterialProduct().getUnitOfMeasure().getAbbreviation());
        r.setQuantityRequired(i.getQuantityRequired());
        r.setNotes(i.getNotes());
        return r;
    }

    private ProductionOrderResponse toProductionOrderResponse(ProductionOrder o) {
        ProductionOrderResponse r = new ProductionOrderResponse();
        r.setId(o.getId());
        r.setProductionNumber(o.getProductionNumber());
        r.setFinishedProductId(o.getFinishedProduct().getId());
        r.setFinishedProductName(o.getFinishedProduct().getName());
        r.setFinishedProductSku(o.getFinishedProduct().getSku());
        r.setBomId(o.getBillOfMaterials().getId());
        r.setBomName(o.getBillOfMaterials().getName());
        r.setStatus(o.getStatus().name());
        r.setPlannedQuantity(o.getPlannedQuantity());
        r.setProducedQuantity(o.getProducedQuantity());
        r.setWarehouseId(o.getWarehouseId());
        r.setPlannedStartDate(o.getPlannedStartDate());
        r.setPlannedEndDate(o.getPlannedEndDate());
        r.setActualStartDate(o.getActualStartDate());
        r.setActualEndDate(o.getActualEndDate());
        r.setNotes(o.getNotes());
        r.setCreatedAt(o.getCreatedAt());
        r.setUpdatedAt(o.getUpdatedAt());
        r.setConsumptions(o.getConsumptions().stream()
                .map(this::toConsumptionResponse).collect(Collectors.toList()));
        warehouseRepository.findById(o.getWarehouseId())
                .ifPresent(w -> r.setWarehouseName(w.getName()));
        return r;
    }

    private ProductionConsumptionResponse toConsumptionResponse(ProductionConsumption c) {
        ProductionConsumptionResponse r = new ProductionConsumptionResponse();
        r.setId(c.getId());
        r.setMaterialProductId(c.getMaterialProduct().getId());
        r.setMaterialProductName(c.getMaterialProduct().getName());
        r.setMaterialProductSku(c.getMaterialProduct().getSku());
        r.setPlannedQuantity(c.getPlannedQuantity());
        r.setConsumedQuantity(c.getConsumedQuantity());
        r.setWarehouseId(c.getWarehouseId());
        return r;
    }
}