package com.brickerp.quality.service;

import com.brickerp.quality.dto.*;
import java.util.List;

public interface QualityService {
    QualityTestResponse createTest(QualityTestRequest request);

    QualityTestResponse getById(Long id);

    List<QualityTestResponse> getAll();

    List<QualityTestResponse> getByProduct(Long productId);

    List<QualityTestResponse> getByProductionOrder(Long productionOrderId);
}