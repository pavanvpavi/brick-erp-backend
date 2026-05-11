package com.brickerp.finance.service;

import com.brickerp.finance.dto.ExpenseRequest;
import com.brickerp.finance.dto.ExpenseResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseService {
    ExpenseResponse create(ExpenseRequest request);

    ExpenseResponse getById(Long id);

    List<ExpenseResponse> getAll();

    List<ExpenseResponse> getByCategory(String category);

    List<ExpenseResponse> getByDateRange(LocalDate start, LocalDate end);

    BigDecimal getTotalExpenses(LocalDate start, LocalDate end);

    void delete(Long id);
}