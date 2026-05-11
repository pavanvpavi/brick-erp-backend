package com.brickerp.product.service;

import com.brickerp.product.dto.ProductRequest;
import com.brickerp.product.dto.ProductResponse;

import java.util.List;

public interface ProductService {
    ProductResponse create(ProductRequest request);

    ProductResponse update(Long id, ProductRequest request);

    ProductResponse getById(Long id);

    ProductResponse getBySku(String sku);

    List<ProductResponse> getAll();

    List<ProductResponse> getAllActive();

    List<ProductResponse> getByCategory(Long categoryId);

    List<ProductResponse> search(String keyword);

    void delete(Long id);
}