package com.brickerp.product.service;

import com.brickerp.product.dto.UomRequest;
import com.brickerp.product.dto.UomResponse;

import java.util.List;

public interface UomService {
    UomResponse create(UomRequest request);

    UomResponse update(Long id, UomRequest request);

    UomResponse getById(Long id);

    List<UomResponse> getAll();

    List<UomResponse> getAllActive();

    void delete(Long id);
}