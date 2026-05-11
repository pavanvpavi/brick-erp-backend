package com.brickerp.finance.repository;

import com.brickerp.finance.entity.Expense;
import com.brickerp.finance.entity.Expense.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findAllByOrderByExpenseDateDesc();

    List<Expense> findByCategoryOrderByExpenseDateDesc(ExpenseCategory category);

    @Query("SELECT e FROM Expense e WHERE e.expenseDate BETWEEN :start AND :end ORDER BY e.expenseDate DESC")
    List<Expense> findByDateRange(LocalDate start, LocalDate end);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.expenseDate BETWEEN :start AND :end")
    java.math.BigDecimal getTotalExpenses(LocalDate start, LocalDate end);

    @Query("SELECT MAX(e.expenseNumber) FROM Expense e WHERE e.expenseNumber LIKE 'EXP-%'")
    Optional<String> findLastExpenseNumber();
}