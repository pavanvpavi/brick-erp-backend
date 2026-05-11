package com.brickerp.product.service.impl;

import com.brickerp.common.exception.BusinessException;
import com.brickerp.common.exception.ResourceNotFoundException;
import com.brickerp.product.dto.ProductCategoryRequest;
import com.brickerp.product.dto.ProductCategoryResponse;
import com.brickerp.product.entity.ProductCategory;
import com.brickerp.product.repository.ProductCategoryRepository;
import com.brickerp.product.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final ProductCategoryRepository categoryRepository;

    @Override
    public ProductCategoryResponse create(ProductCategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new BusinessException("Category with name '" + request.getName() + "' already exists");
        }
        if (request.getCode() != null && categoryRepository.existsByCode(request.getCode())) {
            throw new BusinessException("Category with code '" + request.getCode() + "' already exists");
        }

        ProductCategory category = ProductCategory.builder()
                .name(request.getName())
                .description(request.getDescription())
                .code(request.getCode())
                .build();

        return toResponse(categoryRepository.save(category));
    }

    @Override
    public ProductCategoryResponse update(Long id, ProductCategoryRequest request) {
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductCategory", id));

        // Check duplicates only if value changed
        if (!category.getName().equals(request.getName()) &&
                categoryRepository.existsByName(request.getName())) {
            throw new BusinessException("Category with name '" + request.getName() + "' already exists");
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setCode(request.getCode());

        return toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductCategoryResponse getById(Long id) {
        return categoryRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("ProductCategory", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductCategoryResponse> getAll() {
        return categoryRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductCategoryResponse> getAllActive() {
        return categoryRepository.findByIsActiveTrue()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductCategory", id));
        category.setIsActive(false); // Soft delete
        categoryRepository.save(category);
    }

    private ProductCategoryResponse toResponse(ProductCategory category) {
        ProductCategoryResponse response = new ProductCategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setCode(category.getCode());
        response.setIsActive(category.getIsActive());
        response.setCreatedAt(category.getCreatedAt());
        return response;
    }
}