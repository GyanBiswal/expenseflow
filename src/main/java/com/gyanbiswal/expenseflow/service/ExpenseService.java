package com.gyanbiswal.expenseflow.service;

import com.gyanbiswal.expenseflow.dto.request.ExpenseRequest;
import com.gyanbiswal.expenseflow.dto.response.ExpenseResponse;
import com.gyanbiswal.expenseflow.exception.ResourceNotFoundException;
import com.gyanbiswal.expenseflow.model.Category;
import com.gyanbiswal.expenseflow.model.Expense;
import com.gyanbiswal.expenseflow.model.User;
import com.gyanbiswal.expenseflow.repository.CategoryRepository;
import com.gyanbiswal.expenseflow.repository.ExpenseRepository;
import com.gyanbiswal.expenseflow.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final SecurityUtils securityUtils;

    public ExpenseResponse createExpense(ExpenseRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        // Verify the category belongs to the current user
        Category category = categoryRepository
                .findByIdAndUser(request.getCategoryId(), currentUser)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found: " + request.getCategoryId()));

        Expense expense = Expense.builder()
                .description(request.getDescription())
                .amount(request.getAmount())
                .expenseDate(request.getExpenseDate())
                .category(category)
                .user(currentUser)
                .build();

        Expense saved = expenseRepository.save(expense);
        return toResponse(saved, currentUser);
    }

    public List<ExpenseResponse> getAllExpenses() {
        User currentUser = securityUtils.getCurrentUser();
        return expenseRepository.findByUserOrderByExpenseDateDesc(currentUser)
                .stream()
                .map(e -> toResponse(e, currentUser))
                .toList();
    }

    public ExpenseResponse getExpenseById(Long id) {
        User currentUser = securityUtils.getCurrentUser();
        Expense expense = expenseRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Expense not found: " + id));
        return toResponse(expense, currentUser);
    }

    public ExpenseResponse updateExpense(Long id, ExpenseRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        Expense expense = expenseRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Expense not found: " + id));

        Category category = categoryRepository
                .findByIdAndUser(request.getCategoryId(), currentUser)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found: " + request.getCategoryId()));

        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setCategory(category);

        return toResponse(expenseRepository.save(expense), currentUser);
    }

    public void deleteExpense(Long id) {
        User currentUser = securityUtils.getCurrentUser();
        Expense expense = expenseRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Expense not found: " + id));
        expenseRepository.delete(expense);
    }

    public List<ExpenseResponse> getExpensesByDateRange(
            LocalDate start, LocalDate end) {
        User currentUser = securityUtils.getCurrentUser();
        return expenseRepository
                .findByUserAndExpenseDateBetweenOrderByExpenseDateDesc(
                        currentUser, start, end)
                .stream()
                .map(e -> toResponse(e, currentUser))
                .toList();
    }

    private ExpenseResponse toResponse(Expense expense, User user) {
        // Check if this expense's category is over budget this month
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        BigDecimal spent = expenseRepository.sumByCategoryAndUserAndDateRange(
                expense.getCategory(), user, startOfMonth, endOfMonth);

        boolean exceeded = spent.compareTo(
                expense.getCategory().getBudgetLimit()) >= 0;

        return ExpenseResponse.builder()
                .id(expense.getId())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .expenseDate(expense.getExpenseDate())
                .categoryName(expense.getCategory().getName())
                .budgetExceeded(exceeded)
                .build();
    }
}