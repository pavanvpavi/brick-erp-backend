package com.brickerp.procurement.service;

import com.brickerp.procurement.dto.*;
import java.util.List;

public interface ProcurementService {

    // Supplier
    SupplierResponse createSupplier(SupplierRequest request);

    SupplierResponse updateSupplier(Long id, SupplierRequest request);

    SupplierResponse getSupplierById(Long id);

    List<SupplierResponse> getAllSuppliers();

    void deleteSupplier(Long id);

    // Purchase Orders
    PurchaseOrderResponse createPurchaseOrder(PurchaseOrderRequest request);

    PurchaseOrderResponse getPurchaseOrderById(Long id);

    List<PurchaseOrderResponse> getAllPurchaseOrders();

    List<PurchaseOrderResponse> getPurchaseOrdersBySupplier(Long supplierId);

    List<PurchaseOrderResponse> getPurchaseOrdersByStatus(String status);

    PurchaseOrderResponse sendToSupplier(Long id);

    PurchaseOrderResponse receiveItems(Long id, ReceiveItemsRequest request);

    PurchaseOrderResponse cancelPurchaseOrder(Long id, String reason);
}