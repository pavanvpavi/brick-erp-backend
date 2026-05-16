package com.brickerp.procurement.service.impl;

import com.brickerp.common.exception.BusinessException;
import com.brickerp.common.exception.ResourceNotFoundException;
import com.brickerp.inventory.repository.WarehouseRepository;
import com.brickerp.inventory.service.InventoryService;
import com.brickerp.procurement.dto.*;
import com.brickerp.procurement.entity.PurchaseOrder;
import com.brickerp.procurement.entity.PurchaseOrder.PoStatus;
import com.brickerp.procurement.entity.PurchaseOrderItem;
import com.brickerp.procurement.entity.Supplier;
import com.brickerp.procurement.repository.PurchaseOrderItemRepository;
import com.brickerp.procurement.repository.PurchaseOrderRepository;
import com.brickerp.procurement.repository.SupplierRepository;
import com.brickerp.procurement.service.ProcurementService;
import com.brickerp.product.entity.Product;
import com.brickerp.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.brickerp.procurement.entity.SupplierPriceHistory;
import com.brickerp.procurement.repository.SupplierPriceHistoryRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class ProcurementServiceImpl implements ProcurementService {

    private final SupplierRepository supplierRepository;
    private final PurchaseOrderRepository poRepository;
    private final PurchaseOrderItemRepository poItemRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryService inventoryService;
    private final SupplierPriceHistoryRepository priceHistoryRepository;

    // ==================== SUPPLIER ====================

    @Override
    public SupplierResponse createSupplier(SupplierRequest request) {
        if (request.getGstin() != null && !request.getGstin().isBlank()
                && supplierRepository.existsByGstin(request.getGstin())) {
            throw new BusinessException("Supplier with GSTIN '" + request.getGstin() + "' already exists");
        }

        Supplier supplier = Supplier.builder()
                .supplierCode(generateSupplierCode())
                .name(request.getName())
                .contactPerson(request.getContactPerson())
                .email(request.getEmail())
                .phone(request.getPhone())
                .alternatePhone(request.getAlternatePhone())
                .gstin(request.getGstin())
                .pan(request.getPan())
                .addressLine1(request.getAddressLine1())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .paymentTermsDays(request.getPaymentTermsDays())
                .notes(request.getNotes())
                .build();

        return toSupplierResponse(supplierRepository.save(supplier));
    }

    @Override
    public SupplierResponse updateSupplier(Long id, SupplierRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));

        supplier.setName(request.getName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setAlternatePhone(request.getAlternatePhone());
        supplier.setGstin(request.getGstin());
        supplier.setPan(request.getPan());
        supplier.setAddressLine1(request.getAddressLine1());
        supplier.setCity(request.getCity());
        supplier.setState(request.getState());
        supplier.setPincode(request.getPincode());
        supplier.setPaymentTermsDays(request.getPaymentTermsDays());
        supplier.setNotes(request.getNotes());

        return toSupplierResponse(supplierRepository.save(supplier));
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResponse getSupplierById(Long id) {
        return supplierRepository.findById(id)
                .map(this::toSupplierResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> getAllSuppliers() {
        return supplierRepository.findByIsActiveTrue()
                .stream().map(this::toSupplierResponse).collect(Collectors.toList());
    }

    @Override
    public void deleteSupplier(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));
        supplier.setIsActive(false);
        supplierRepository.save(supplier);
    }

    // ==================== PURCHASE ORDER ====================

    @Override
    public PurchaseOrderResponse createPurchaseOrder(PurchaseOrderRequest request) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", request.getSupplierId()));

        warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", request.getWarehouseId()));

        PurchaseOrder po = PurchaseOrder.builder()
                .poNumber(generatePoNumber())
                .supplier(supplier)
                .warehouseId(request.getWarehouseId())
                .orderDate(LocalDate.now())
                .expectedDeliveryDate(request.getExpectedDeliveryDate())
                .status(PoStatus.DRAFT)
                .notes(request.getNotes())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        for (PurchaseOrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", itemReq.getProductId()));

            BigDecimal taxPct = itemReq.getTaxPercentage() != null
                    ? itemReq.getTaxPercentage()
                    : BigDecimal.ZERO;
            BigDecimal baseAmount = itemReq.getUnitPrice()
                    .multiply(BigDecimal.valueOf(itemReq.getQuantityOrdered()));
            BigDecimal taxAmt = baseAmount.multiply(taxPct)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal lineTotal = baseAmount.add(taxAmt);

            PurchaseOrderItem item = PurchaseOrderItem.builder()
                    .purchaseOrder(po)
                    .product(product)
                    .quantityOrdered(itemReq.getQuantityOrdered())
                    .quantityReceived(0)
                    .unitPrice(itemReq.getUnitPrice())
                    .taxPercentage(taxPct)
                    .taxAmount(taxAmt)
                    .lineTotal(lineTotal)
                    .notes(itemReq.getNotes())
                    .build();

            po.getItems().add(item);
            subtotal = subtotal.add(baseAmount);
            totalTax = totalTax.add(taxAmt);
        }

        po.setSubtotal(subtotal);
        po.setTaxAmount(totalTax);
        po.setTotalAmount(subtotal.add(totalTax));

        // Auto record price history BEFORE saving
        // Loop through original items list (not savedPo)
        for (PurchaseOrderItem item : po.getItems()) {
            SupplierPriceHistory history = SupplierPriceHistory.builder()
                    .supplier(supplier)
                    .product(item.getProduct())
                    .unitPrice(item.getUnitPrice())
                    .effectiveDate(LocalDate.now())
                    .poNumber(po.getPoNumber())
                    .build();
            priceHistoryRepository.save(history);
        }

        return toPurchaseOrderResponse(poRepository.save(po));
    }

    @Override
    public PurchaseOrderResponse sendToSupplier(Long id) {
        PurchaseOrder po = poRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id));

        if (po.getStatus() != PoStatus.DRAFT) {
            throw new BusinessException("Only DRAFT purchase orders can be sent");
        }

        po.setStatus(PoStatus.SENT);
        return toPurchaseOrderResponse(poRepository.save(po));
    }

    @Override
    public PurchaseOrderResponse receiveItems(Long id, ReceiveItemsRequest request) {
        PurchaseOrder po = poRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id));

        if (po.getStatus() == PoStatus.CANCELLED) {
            throw new BusinessException("Cannot receive items for a cancelled purchase order");
        }
        if (po.getStatus() == PoStatus.RECEIVED) {
            throw new BusinessException("Purchase order is already fully received");
        }

        for (ReceiveItemsRequest.ReceivedItem receivedItem : request.getItems()) {
            PurchaseOrderItem poItem = poItemRepository.findById(receivedItem.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrderItem",
                            receivedItem.getItemId()));

            if (receivedItem.getReceivedQuantity() > poItem.getPendingQuantity()) {
                throw new BusinessException("Received quantity ("
                        + receivedItem.getReceivedQuantity()
                        + ") exceeds pending quantity ("
                        + poItem.getPendingQuantity() + ") for product: "
                        + poItem.getProduct().getName());
            }

            poItem.setQuantityReceived(
                    poItem.getQuantityReceived() + receivedItem.getReceivedQuantity());
            poItemRepository.save(poItem);

            // Add to inventory
            inventoryService.addStock(
                    poItem.getProduct().getId(),
                    po.getWarehouseId(),
                    receivedItem.getReceivedQuantity(),
                    "PURCHASE_ORDER",
                    po.getId(),
                    request.getNotes() != null ? request.getNotes()
                            : "Received against PO: " + po.getPoNumber());
        }

        // Update PO status
        boolean allReceived = po.getItems().stream()
                .allMatch(item -> item.getPendingQuantity() == 0);
        po.setStatus(allReceived ? PoStatus.RECEIVED : PoStatus.PARTIALLY_RECEIVED);
        if (allReceived)
            po.setReceivedDate(LocalDate.now());

        return toPurchaseOrderResponse(poRepository.save(po));
    }

    @Override
    public PurchaseOrderResponse cancelPurchaseOrder(Long id, String reason) {
        PurchaseOrder po = poRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id));

        if (po.getStatus() == PoStatus.RECEIVED) {
            throw new BusinessException("Cannot cancel a fully received purchase order");
        }
        if (po.getStatus() == PoStatus.CANCELLED) {
            throw new BusinessException("Purchase order is already cancelled");
        }

        po.setStatus(PoStatus.CANCELLED);
        po.setNotes(reason != null ? reason : po.getNotes());
        return toPurchaseOrderResponse(poRepository.save(po));
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderResponse getPurchaseOrderById(Long id) {
        return poRepository.findById(id)
                .map(this::toPurchaseOrderResponse)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> getAllPurchaseOrders() {
        return poRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toPurchaseOrderResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> getPurchaseOrdersBySupplier(Long supplierId) {
        return poRepository.findBySupplierIdOrderByCreatedAtDesc(supplierId)
                .stream().map(this::toPurchaseOrderResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> getPurchaseOrdersByStatus(String status) {
        try {
            PoStatus poStatus = PoStatus.valueOf(status.toUpperCase());
            return poRepository.findByStatusOrderByCreatedAtDesc(poStatus)
                    .stream().map(this::toPurchaseOrderResponse).collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid status: " + status);
        }
    }

    // ==================== PRIVATE HELPERS ====================

    private String generateSupplierCode() {
        return supplierRepository.findLastSupplierCode()
                .map(last -> {
                    int num = Integer.parseInt(last.replace("SUP-", ""));
                    return String.format("SUP-%04d", num + 1);
                })
                .orElse("SUP-0001");
    }

    private String generatePoNumber() {
        return poRepository.findLastPoNumber()
                .map(last -> {
                    int num = Integer.parseInt(last.replace("PO-", ""));
                    return String.format("PO-%05d", num + 1);
                })
                .orElse("PO-00001");
    }

    private SupplierResponse toSupplierResponse(Supplier s) {
        SupplierResponse r = new SupplierResponse();
        r.setId(s.getId());
        r.setSupplierCode(s.getSupplierCode());
        r.setName(s.getName());
        r.setContactPerson(s.getContactPerson());
        r.setEmail(s.getEmail());
        r.setPhone(s.getPhone());
        r.setGstin(s.getGstin());
        r.setPan(s.getPan());
        r.setAddressLine1(s.getAddressLine1());
        r.setCity(s.getCity());
        r.setState(s.getState());
        r.setPincode(s.getPincode());
        r.setPaymentTermsDays(s.getPaymentTermsDays());
        r.setNotes(s.getNotes());
        r.setIsActive(s.getIsActive());
        r.setCreatedAt(s.getCreatedAt());
        return r;
    }

    private PurchaseOrderResponse toPurchaseOrderResponse(PurchaseOrder po) {
        PurchaseOrderResponse r = new PurchaseOrderResponse();
        r.setId(po.getId());
        r.setPoNumber(po.getPoNumber());
        r.setSupplierId(po.getSupplier().getId());
        r.setSupplierName(po.getSupplier().getName());
        r.setStatus(po.getStatus().name());
        r.setOrderDate(po.getOrderDate());
        r.setExpectedDeliveryDate(po.getExpectedDeliveryDate());
        r.setReceivedDate(po.getReceivedDate());
        r.setWarehouseId(po.getWarehouseId());
        r.setSubtotal(po.getSubtotal());
        r.setTaxAmount(po.getTaxAmount());
        r.setTotalAmount(po.getTotalAmount());
        r.setNotes(po.getNotes());
        r.setCreatedAt(po.getCreatedAt());
        r.setUpdatedAt(po.getUpdatedAt());
        r.setItems(po.getItems().stream().map(this::toItemResponse).collect(Collectors.toList()));
        warehouseRepository.findById(po.getWarehouseId())
                .ifPresent(w -> r.setWarehouseName(w.getName()));
        return r;
    }

    private PurchaseOrderItemResponse toItemResponse(PurchaseOrderItem i) {
        PurchaseOrderItemResponse r = new PurchaseOrderItemResponse();
        r.setId(i.getId());
        r.setProductId(i.getProduct().getId());
        r.setProductName(i.getProduct().getName());
        r.setProductSku(i.getProduct().getSku());
        r.setQuantityOrdered(i.getQuantityOrdered());
        r.setQuantityReceived(i.getQuantityReceived());
        r.setPendingQuantity(i.getPendingQuantity());
        r.setUnitPrice(i.getUnitPrice());
        r.setTaxPercentage(i.getTaxPercentage());
        r.setTaxAmount(i.getTaxAmount());
        r.setLineTotal(i.getLineTotal());
        r.setNotes(i.getNotes());
        return r;
    }
}