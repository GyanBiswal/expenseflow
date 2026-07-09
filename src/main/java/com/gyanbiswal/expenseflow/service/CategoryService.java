package com.gyanbiswal.expenseflow.service;

import com.gyanbiswal.expenseflow.dto.request.CategoryRequest;
import com.gyanbiswal.expenseflow.dto.response.CategoryResponse;
import com.gyanbiswal.expenseflow.exception.ResourceNotFoundException;
import com.gyanbiswal.expenseflow.model.Category;
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
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final SecurityUtils securityUtils;

    public CategoryResponse createCategory(CategoryRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        Category category = Category.builder()
                .name(request.getName())
                .budgetLimit(request.getBudgetLimit())
                .user(currentUser)
                .build();

        Category saved = categoryRepository.save(category);
        return toResponse(saved, currentUser);
    }

    public List<CategoryResponse> getAllCategories() {
        User currentUser = securityUtils.getCurrentUser();
        return categoryRepository.findByUser(currentUser)
                .stream()
                .map(cat -> toResponse(cat, currentUser))
                .toList();
    }

    public CategoryResponse getCategoryById(Long id) {
        User currentUser = securityUtils.getCurrentUser();
        Category category = categoryRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found: " + id));
        return toResponse(category, currentUser);
    }

    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        Category category = categoryRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found: " + id));

        category.setName(request.getName());
        category.setBudgetLimit(request.getBudgetLimit());

        return toResponse(categoryRepository.save(category), currentUser);
    }

    public void deleteCategory(Long id) {
        User currentUser = securityUtils.getCurrentUser();
        Category category = categoryRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found: " + id));
        categoryRepository.delete(category);
    }

    // Converts entity to response DTO
    // Also calculates monthly spend and breach status — this is the business logic
    private CategoryResponse toResponse(Category category, User user) {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        // Run the aggregation query for this category this month
        BigDecimal spent = expenseRepository.sumByCategoryAndUserAndDateRange(
                category, user, startOfMonth, endOfMonth);

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .budgetLimit(category.getBudgetLimit())
                .spentThisMonth(spent)
                // Budget exceeded if spent >= budgetLimit
                .budgetExceeded(spent.compareTo(category.getBudgetLimit()) >= 0)
                .build();
    }
}