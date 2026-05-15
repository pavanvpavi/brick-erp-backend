package com.brickerp.customer.service;

import com.brickerp.customer.dto.*;

import java.util.List;

public interface CustomerService {
    CustomerResponse create(CustomerRequest request);

    CustomerResponse update(Long id, CustomerRequest request);

    CustomerResponse getById(Long id);

    CustomerResponse getByCode(String code);

    List<CustomerSummaryResponse> getAll();

    List<CustomerSummaryResponse> search(String keyword);

    void delete(Long id);

    // Address management
    CustomerResponse addAddress(Long customerId, CustomerAddressRequest request);

    CustomerResponse updateAddress(Long customerId, Long addressId, CustomerAddressRequest request);

    void deleteAddress(Long customerId, Long addressId);

    CustomerLedgerResponse getCustomerLedger(Long customerId);
}