package com.brickerp.product.service.impl;

import com.brickerp.common.exception.BusinessException;
import com.brickerp.common.exception.ResourceNotFoundException;
import com.brickerp.product.dto.ProductRequest;
import com.brickerp.product.dto.ProductResponse;
import com.brickerp.product.entity.Product;
import com.brickerp.product.entity.ProductCategory;
import com.brickerp.product.entity.UnitOfMeasure;
import com.brickerp.product.repository.ProductCategoryRepository;
import com.brickerp.product.repository.ProductRepository;
import com.brickerp.product.repository.UnitOfMeasureRepository;
import com.brickerp.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final UnitOfMeasureRepository uomRepository;

    @Override
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new BusinessException("Product with SKU '" + request.getSku() + "' already exists");
        }

        ProductCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("ProductCategory", request.getCategoryId()));

        UnitOfMeasure uom = uomRepository.findById(request.getUomId())
                .orElseThrow(() -> new ResourceNotFoundException("UnitOfMeasure", request.getUomId()));

        Product product = Product.builder()
                .sku(request.getSku())
                .name(request.getName())
                .description(request.getDescription())
                .category(category)
                .unitOfMeasure(uom)
                .lengthMm(request.getLengthMm())
                .widthMm(request.getWidthMm())
                .heightMm(request.getHeightMm())
                .weightKg(request.getWeightKg())
                .strengthGrade(request.getStrengthGrade())
                .material(request.getMaterial())
                .color(request.getColor())
                .sellingPrice(request.getSellingPrice())
                .costPrice(request.getCostPrice())
                .taxPercentage(
                        request.getTaxPercentage() != null ? request.getTaxPercentage() : java.math.BigDecimal.ZERO)
                .minimumStockLevel(request.getMinimumStockLevel() != null ? request.getMinimumStockLevel() : 0)
                .reorderQuantity(request.getReorderQuantity() != null ? request.getReorderQuantity() : 0)
                .build();

        return toResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        if (!product.getSku().equals(request.getSku()) &&
                productRepository.existsBySku(request.getSku())) {
            throw new BusinessException("Product with SKU '" + request.getSku() + "' already exists");
        }

        ProductCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("ProductCategory", request.getCategoryId()));

        UnitOfMeasure uom = uomRepository.findById(request.getUomId())
                .orElseThrow(() -> new ResourceNotFoundException("UnitOfMeasure", request.getUomId()));

        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(category);
        product.setUnitOfMeasure(uom);
        product.setLengthMm(request.getLengthMm());
        product.setWidthMm(request.getWidthMm());
        product.setHeightMm(request.getHeightMm());
        product.setWeightKg(request.getWeightKg());
        product.setStrengthGrade(request.getStrengthGrade());
        product.setMaterial(request.getMaterial());
        product.setColor(request.getColor());
        product.setSellingPrice(request.getSellingPrice());
        product.setCostPrice(request.getCostPrice());
        if (request.getTaxPercentage() != null)
            product.setTaxPercentage(request.getTaxPercentage());
        if (request.getMinimumStockLevel() != null)
            product.setMinimumStockLevel(request.getMinimumStockLevel());
        if (request.getReorderQuantity() != null)
            product.setReorderQuantity(request.getReorderQuantity());

        return toResponse(productRepository.save(product));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return productRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getBySku(String sku) {
        return productRepository.findBySku(sku)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with SKU: " + sku));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAll() {
        return productRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllActive() {
        return productRepository.findByIsActiveTrue()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getByCategory(Long categoryId) {
        return productRepository.findByCategoryIdAndIsActiveTrue(categoryId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> search(String keyword) {
        return productRepository.searchByKeyword(keyword)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        product.setIsActive(false);
        productRepository.save(product);
    }

    private ProductResponse toResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setSku(product.getSku());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setCategoryId(product.getCategory().getId());
        response.setCategoryName(product.getCategory().getName());
        response.setUomId(product.getUnitOfMeasure().getId());
        response.setUomName(product.getUnitOfMeasure().getName());
        response.setUomAbbreviation(product.getUnitOfMeasure().getAbbreviation());
        response.setLengthMm(product.getLengthMm());
        response.setWidthMm(product.getWidthMm());
        response.setHeightMm(product.getHeightMm());
        response.setWeightKg(product.getWeightKg());
        response.setStrengthGrade(product.getStrengthGrade());
        response.setMaterial(product.getMaterial());
        response.setColor(product.getColor());
        response.setSellingPrice(product.getSellingPrice());
        response.setCostPrice(product.getCostPrice());
        response.setTaxPercentage(product.getTaxPercentage());
        response.setMinimumStockLevel(product.getMinimumStockLevel());
        response.setReorderQuantity(product.getReorderQuantity());
        response.setIsActive(product.getIsActive());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        return response;
    }
}