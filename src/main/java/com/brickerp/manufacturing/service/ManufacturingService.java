package com.brickerp.manufacturing.service;

import com.brickerp.manufacturing.dto.*;
import java.util.List;

public interface ManufacturingService {

    // BOM
    BomResponse createBom(BomRequest request);

    BomResponse getBomById(Long id);

    List<BomResponse> getAllBoms();

    List<BomResponse> getBomsByProduct(Long productId);

    void deleteBom(Long id);

    // Production Orders
    ProductionOrderResponse createProductionOrder(ProductionOrderRequest request);

    ProductionOrderResponse getProductionOrderById(Long id);

    List<ProductionOrderResponse> getAllProductionOrders();

    List<ProductionOrderResponse> getProductionOrdersByStatus(String status);

    ProductionOrderResponse startProduction(Long id);

    ProductionOrderResponse completeProduction(Long id, CompleteProductionRequest request);

    ProductionOrderResponse cancelProductionOrder(Long id, String reason);
}