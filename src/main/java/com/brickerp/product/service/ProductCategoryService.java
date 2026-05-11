package com.brickerp.product.service;

import com.brickerp.product.dto.ProductCategoryRequest;
import com.brickerp.product.dto.ProductCategoryResponse;

import java.util.List;

public interface ProductCategoryService {
    ProductCategoryResponse create(ProductCategoryRequest request);

    ProductCategoryResponse update(Long id, ProductCategoryRequest request);

    ProductCategoryResponse getById(Long id);

    List<ProductCategoryResponse> getAll();

    List<ProductCategoryResponse> getAllActive();

    void delete(Long id);
}