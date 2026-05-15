package com.brickerp.customer.service.impl;

import com.brickerp.common.exception.BusinessException;
import com.brickerp.common.exception.ResourceNotFoundException;
import com.brickerp.customer.dto.*;
import com.brickerp.customer.entity.Customer;
import com.brickerp.customer.entity.Customer.CustomerType;
import com.brickerp.customer.entity.CustomerAddress;
import com.brickerp.customer.entity.CustomerAddress.AddressType;
import com.brickerp.customer.repository.CustomerAddressRepository;
import com.brickerp.customer.repository.CustomerRepository;
import com.brickerp.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import com.brickerp.finance.entity.Invoice;
import com.brickerp.finance.entity.Payment;
import com.brickerp.finance.repository.InvoiceRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository addressRepository;
    private final InvoiceRepository invoiceRepository;

    @Override
    public CustomerResponse create(CustomerRequest request) {
        if (request.getGstin() != null && !request.getGstin().isBlank()
                && customerRepository.existsByGstin(request.getGstin())) {
            throw new BusinessException("Customer with GSTIN '" + request.getGstin() + "' already exists");
        }

        Customer customer = Customer.builder()
                .customerCode(generateCustomerCode())
                .name(request.getName())
                .customerType(CustomerType.valueOf(request.getCustomerType()))
                .email(request.getEmail())
                .phone(request.getPhone())
                .alternatePhone(request.getAlternatePhone())
                .gstin(request.getGstin())
                .pan(request.getPan())
                .creditLimit(request.getCreditLimit())
                .creditDays(request.getCreditDays())
                .notes(request.getNotes())
                .build();

        Customer saved = customerRepository.save(customer);

        if (request.getAddresses() != null) {
            for (CustomerAddressRequest addrReq : request.getAddresses()) {
                CustomerAddress address = toAddressEntity(addrReq, saved);
                saved.getAddresses().add(address);
            }
            customerRepository.save(saved);
        }

        return toResponse(saved);
    }

    @Override
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));

        customer.setName(request.getName());
        customer.setCustomerType(CustomerType.valueOf(request.getCustomerType()));
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAlternatePhone(request.getAlternatePhone());
        customer.setGstin(request.getGstin());
        customer.setPan(request.getPan());
        customer.setCreditLimit(request.getCreditLimit());
        customer.setCreditDays(request.getCreditDays());
        customer.setNotes(request.getNotes());

        return toResponse(customerRepository.save(customer));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getById(Long id) {
        return customerRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getByCode(String code) {
        return customerRepository.findByCustomerCode(code)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with code: " + code));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerSummaryResponse> getAll() {
        return customerRepository.findByIsActiveTrue()
                .stream().map(this::toSummaryResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerSummaryResponse> search(String keyword) {
        return customerRepository.searchCustomers(keyword)
                .stream().map(this::toSummaryResponse).collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        customer.setIsActive(false);
        customerRepository.save(customer);
    }

    @Override
    public CustomerResponse addAddress(Long customerId, CustomerAddressRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));

        CustomerAddress address = toAddressEntity(request, customer);
        customer.getAddresses().add(address);
        return toResponse(customerRepository.save(customer));
    }

    @Override
    public CustomerResponse updateAddress(Long customerId, Long addressId,
            CustomerAddressRequest request) {
        CustomerAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerAddress", addressId));

        address.setAddressType(AddressType.valueOf(request.getAddressType()));
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPincode(request.getPincode());
        address.setCountry(request.getCountry());
        address.setIsDefault(request.getIsDefault());
        address.setContactName(request.getContactName());
        address.setContactPhone(request.getContactPhone());
        addressRepository.save(address);

        return getById(customerId);
    }

    @Override
    public void deleteAddress(Long customerId, Long addressId) {
        CustomerAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerAddress", addressId));
        addressRepository.delete(address);
    }

    // ==================== PRIVATE HELPERS ====================

    private String generateCustomerCode() {
        return customerRepository.findLastCustomerCode()
                .map(last -> {
                    int num = Integer.parseInt(last.replace("CUST-", ""));
                    return String.format("CUST-%04d", num + 1);
                })
                .orElse("CUST-0001");
    }

    private CustomerAddress toAddressEntity(CustomerAddressRequest req, Customer customer) {
        return CustomerAddress.builder()
                .customer(customer)
                .addressType(AddressType.valueOf(req.getAddressType()))
                .addressLine1(req.getAddressLine1())
                .addressLine2(req.getAddressLine2())
                .city(req.getCity())
                .state(req.getState())
                .pincode(req.getPincode())
                .country(req.getCountry() != null ? req.getCountry() : "India")
                .isDefault(req.getIsDefault() != null ? req.getIsDefault() : false)
                .contactName(req.getContactName())
                .contactPhone(req.getContactPhone())
                .build();
    }

    private CustomerResponse toResponse(Customer c) {
        CustomerResponse r = new CustomerResponse();
        r.setId(c.getId());
        r.setCustomerCode(c.getCustomerCode());
        r.setName(c.getName());
        r.setCustomerType(c.getCustomerType().name());
        r.setEmail(c.getEmail());
        r.setPhone(c.getPhone());
        r.setAlternatePhone(c.getAlternatePhone());
        r.setGstin(c.getGstin());
        r.setPan(c.getPan());
        r.setCreditLimit(c.getCreditLimit());
        r.setCreditDays(c.getCreditDays());
        r.setNotes(c.getNotes());
        r.setIsActive(c.getIsActive());
        r.setCreatedAt(c.getCreatedAt());
        r.setUpdatedAt(c.getUpdatedAt());
        r.setAddresses(c.getAddresses().stream()
                .map(this::toAddressResponse).collect(Collectors.toList()));
        return r;
    }

    private CustomerSummaryResponse toSummaryResponse(Customer c) {
        CustomerSummaryResponse r = new CustomerSummaryResponse();
        r.setId(c.getId());
        r.setCustomerCode(c.getCustomerCode());
        r.setName(c.getName());
        r.setCustomerType(c.getCustomerType().name());
        r.setPhone(c.getPhone());
        r.setEmail(c.getEmail());
        r.setIsActive(c.getIsActive());
        // Get city from first address if exists
        if (!c.getAddresses().isEmpty()) {
            r.setCity(c.getAddresses().get(0).getCity());
        }
        return r;
    }

    private CustomerAddressResponse toAddressResponse(CustomerAddress a) {
        CustomerAddressResponse r = new CustomerAddressResponse();
        r.setId(a.getId());
        r.setAddressType(a.getAddressType().name());
        r.setAddressLine1(a.getAddressLine1());
        r.setAddressLine2(a.getAddressLine2());
        r.setCity(a.getCity());
        r.setState(a.getState());
        r.setPincode(a.getPincode());
        r.setCountry(a.getCountry());
        r.setIsDefault(a.getIsDefault());
        r.setContactName(a.getContactName());
        r.setContactPhone(a.getContactPhone());
        return r;
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerLedgerResponse getCustomerLedger(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));

        List<Invoice> invoices = invoiceRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);

        List<LedgerEntryResponse> entries = new ArrayList<>();
        BigDecimal balance = BigDecimal.ZERO;
        BigDecimal totalInvoiced = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;

        // Collect all entries (invoices + payments) sorted by date
        for (Invoice invoice : invoices) {
            if (invoice.getStatus() == Invoice.InvoiceStatus.CANCELLED)
                continue;

            // Invoice entry — debit
            totalInvoiced = totalInvoiced.add(invoice.getTotalAmount());
            balance = balance.add(invoice.getTotalAmount());

            entries.add(LedgerEntryResponse.builder()
                    .date(invoice.getInvoiceDate().toString())
                    .type("INVOICE")
                    .referenceNumber(invoice.getInvoiceNumber())
                    .description("Invoice for order: " +
                            (invoice.getSalesOrder() != null
                                    ? invoice.getSalesOrder().getOrderNumber()
                                    : "—"))
                    .debit(invoice.getTotalAmount())
                    .credit(BigDecimal.ZERO)
                    .balance(balance)
                    .build());

            // Payment entries — credit
            for (Payment payment : invoice.getPayments()) {
                totalPaid = totalPaid.add(payment.getAmount());
                balance = balance.subtract(payment.getAmount());

                entries.add(LedgerEntryResponse.builder()
                        .date(payment.getPaymentDate().toString())
                        .type("PAYMENT")
                        .referenceNumber(payment.getPaymentNumber())
                        .description("Payment via " + payment.getPaymentMethod().name()
                                + (payment.getReferenceNumber() != null
                                        ? " | Ref: " + payment.getReferenceNumber()
                                        : ""))
                        .debit(BigDecimal.ZERO)
                        .credit(payment.getAmount())
                        .balance(balance)
                        .build());
            }
        }

        // Sort by date
        entries.sort(Comparator.comparing(LedgerEntryResponse::getDate));

        return CustomerLedgerResponse.builder()
                .customerId(customer.getId())
                .customerName(customer.getName())
                .customerCode(customer.getCustomerCode())
                .totalInvoiced(totalInvoiced)
                .totalPaid(totalPaid)
                .totalOutstanding(totalInvoiced.subtract(totalPaid))
                .entries(entries)
                .build();
    }
}