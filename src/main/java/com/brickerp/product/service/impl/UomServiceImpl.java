package com.brickerp.product.service.impl;

import com.brickerp.common.exception.BusinessException;
import com.brickerp.common.exception.ResourceNotFoundException;
import com.brickerp.product.dto.UomRequest;
import com.brickerp.product.dto.UomResponse;
import com.brickerp.product.entity.UnitOfMeasure;
import com.brickerp.product.repository.UnitOfMeasureRepository;
import com.brickerp.product.service.UomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UomServiceImpl implements UomService {

    private final UnitOfMeasureRepository uomRepository;

    @Override
    public UomResponse create(UomRequest request) {
        if (uomRepository.existsByName(request.getName())) {
            throw new BusinessException("UOM with name '" + request.getName() + "' already exists");
        }
        if (uomRepository.existsByAbbreviation(request.getAbbreviation())) {
            throw new BusinessException("UOM with abbreviation '" + request.getAbbreviation() + "' already exists");
        }

        UnitOfMeasure uom = UnitOfMeasure.builder()
                .name(request.getName())
                .abbreviation(request.getAbbreviation().toUpperCase())
                .description(request.getDescription())
                .build();

        return toResponse(uomRepository.save(uom));
    }

    @Override
    public UomResponse update(Long id, UomRequest request) {
        UnitOfMeasure uom = uomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UnitOfMeasure", id));

        uom.setName(request.getName());
        uom.setAbbreviation(request.getAbbreviation().toUpperCase());
        uom.setDescription(request.getDescription());

        return toResponse(uomRepository.save(uom));
    }

    @Override
    @Transactional(readOnly = true)
    public UomResponse getById(Long id) {
        return uomRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("UnitOfMeasure", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UomResponse> getAll() {
        return uomRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UomResponse> getAllActive() {
        return uomRepository.findByIsActiveTrue()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        UnitOfMeasure uom = uomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UnitOfMeasure", id));
        uom.setIsActive(false);
        uomRepository.save(uom);
    }

    private UomResponse toResponse(UnitOfMeasure uom) {
        UomResponse response = new UomResponse();
        response.setId(uom.getId());
        response.setName(uom.getName());
        response.setAbbreviation(uom.getAbbreviation());
        response.setDescription(uom.getDescription());
        response.setIsActive(uom.getIsActive());
        return response;
    }
}