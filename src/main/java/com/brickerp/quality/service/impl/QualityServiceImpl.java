package com.brickerp.quality.service.impl;

import com.brickerp.common.exception.ResourceNotFoundException;
import com.brickerp.manufacturing.entity.ProductionOrder;
import com.brickerp.manufacturing.repository.ProductionOrderRepository;
import com.brickerp.product.entity.Product;
import com.brickerp.product.repository.ProductRepository;
import com.brickerp.quality.dto.*;
import com.brickerp.quality.entity.QualityTest;
import com.brickerp.quality.entity.QualityTest.TestResult;
import com.brickerp.quality.repository.QualityTestRepository;
import com.brickerp.quality.service.QualityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QualityServiceImpl implements QualityService {

    private final QualityTestRepository qualityRepository;
    private final ProductRepository productRepository;
    private final ProductionOrderRepository productionOrderRepository;

    @Override
    public QualityTestResponse createTest(QualityTestRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));

        ProductionOrder productionOrder = null;
        if (request.getProductionOrderId() != null) {
            productionOrder = productionOrderRepository.findById(request.getProductionOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("ProductionOrder",
                            request.getProductionOrderId()));
        }

        TestResult result;
        if (request.getRejectedQuantity() == 0) {
            result = TestResult.PASS;
        } else if (request.getPassedQuantity() == 0) {
            result = TestResult.FAIL;
        } else {
            result = TestResult.PARTIAL;
        }

        QualityTest test = QualityTest.builder()
                .testNumber(generateTestNumber())
                .product(product)
                .productionOrder(productionOrder)
                .testDate(request.getTestDate())
                .batchSize(request.getBatchSize())
                .passedQuantity(request.getPassedQuantity())
                .rejectedQuantity(request.getRejectedQuantity())
                .rejectionReason(request.getRejectionReason())
                .result(result)
                .compressiveStrength(request.getCompressiveStrength())
                .waterAbsorptionPercentage(request.getWaterAbsorptionPercentage())
                .efflorescence(request.getEfflorescence())
                .testedBy(request.getTestedBy())
                .notes(request.getNotes())
                .build();

        return toResponse(qualityRepository.save(test));
    }

    @Override
    @Transactional(readOnly = true)
    public QualityTestResponse getById(Long id) {
        return qualityRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("QualityTest", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<QualityTestResponse> getAll() {
        return qualityRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QualityTestResponse> getByProduct(Long productId) {
        return qualityRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QualityTestResponse> getByProductionOrder(Long productionOrderId) {
        return qualityRepository.findByProductionOrderIdOrderByCreatedAtDesc(productionOrderId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private String generateTestNumber() {
        return qualityRepository.findLastTestNumber()
                .map(last -> {
                    int num = Integer.parseInt(last.replace("QT-", ""));
                    return String.format("QT-%05d", num + 1);
                })
                .orElse("QT-00001");
    }

    private QualityTestResponse toResponse(QualityTest t) {
        QualityTestResponse r = new QualityTestResponse();
        r.setId(t.getId());
        r.setTestNumber(t.getTestNumber());
        r.setProductId(t.getProduct().getId());
        r.setProductName(t.getProduct().getName());
        if (t.getProductionOrder() != null) {
            r.setProductionOrderId(t.getProductionOrder().getId());
            r.setProductionNumber(t.getProductionOrder().getProductionNumber());
        }
        r.setTestDate(t.getTestDate());
        r.setBatchSize(t.getBatchSize());
        r.setPassedQuantity(t.getPassedQuantity());
        r.setRejectedQuantity(t.getRejectedQuantity());
        r.setPassRate(t.getBatchSize() > 0
                ? (double) t.getPassedQuantity() / t.getBatchSize() * 100
                : 0);
        r.setRejectionReason(t.getRejectionReason());
        r.setResult(t.getResult().name());
        r.setCompressiveStrength(t.getCompressiveStrength());
        r.setWaterAbsorptionPercentage(t.getWaterAbsorptionPercentage());
        r.setEfflorescence(t.getEfflorescence());
        r.setTestedBy(t.getTestedBy());
        r.setNotes(t.getNotes());
        r.setCreatedAt(t.getCreatedAt());
        return r;
    }
}