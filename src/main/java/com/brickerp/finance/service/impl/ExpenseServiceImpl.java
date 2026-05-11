package com.brickerp.finance.service.impl;

import com.brickerp.common.exception.ResourceNotFoundException;
import com.brickerp.finance.dto.ExpenseRequest;
import com.brickerp.finance.dto.ExpenseResponse;
import com.brickerp.finance.entity.Expense;
import com.brickerp.finance.entity.Expense.ExpenseCategory;
import com.brickerp.finance.repository.ExpenseRepository;
import com.brickerp.finance.service.ExpenseService;
import com.brickerp.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Override
    public ExpenseResponse create(ExpenseRequest request) {
        ExpenseCategory category;
        try {
            category = ExpenseCategory.valueOf(request.getCategory().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid category: " + request.getCategory());
        }

        Expense expense = Expense.builder()
                .expenseNumber(generateExpenseNumber())
                .category(category)
                .description(request.getDescription())
                .amount(request.getAmount())
                .expenseDate(request.getExpenseDate())
                .paidTo(request.getPaidTo())
                .paymentMethod(request.getPaymentMethod())
                .referenceNumber(request.getReferenceNumber())
                .notes(request.getNotes())
                .build();

        return toResponse(expenseRepository.save(expense));
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseResponse getById(Long id) {
        return expenseRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getAll() {
        return expenseRepository.findAllByOrderByExpenseDateDesc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getByCategory(String category) {
        try {
            ExpenseCategory cat = ExpenseCategory.valueOf(category.toUpperCase());
            return expenseRepository.findByCategoryOrderByExpenseDateDesc(cat)
                    .stream().map(this::toResponse).collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid category: " + category);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getByDateRange(LocalDate start, LocalDate end) {
        return expenseRepository.findByDateRange(start, end)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalExpenses(LocalDate start, LocalDate end) {
        return expenseRepository.getTotalExpenses(start, end);
    }

    @Override
    public void delete(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", id));
        expense.setIsActive(false);
        expenseRepository.save(expense);
    }

    private String generateExpenseNumber() {
        return expenseRepository.findLastExpenseNumber()
                .map(last -> {
                    int num = Integer.parseInt(last.replace("EXP-", ""));
                    return String.format("EXP-%05d", num + 1);
                })
                .orElse("EXP-00001");
    }

    private ExpenseResponse toResponse(Expense e) {
        ExpenseResponse r = new ExpenseResponse();
        r.setId(e.getId());
        r.setExpenseNumber(e.getExpenseNumber());
        r.setCategory(e.getCategory().name());
        r.setDescription(e.getDescription());
        r.setAmount(e.getAmount());
        r.setExpenseDate(e.getExpenseDate());
        r.setPaidTo(e.getPaidTo());
        r.setPaymentMethod(e.getPaymentMethod());
        r.setReferenceNumber(e.getReferenceNumber());
        r.setNotes(e.getNotes());
        r.setCreatedAt(e.getCreatedAt());
        return r;
    }
}