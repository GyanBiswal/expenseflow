package com.gyanbiswal.expenseflow.repository;

import com.gyanbiswal.expenseflow.model.Category;
import com.gyanbiswal.expenseflow.model.Expense;
import com.gyanbiswal.expenseflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // Paginated version of getAllExpenses
    Page<Expense> findByUser(User user, Pageable pageable);

    // All expenses for a user — for listing
    List<Expense> findByUserOrderByExpenseDateDesc(User user);

    // Find specific expense by id AND user — ownership check
    Optional<Expense> findByIdAndUser(Long id, User user);

    // All expenses for a user within a date range — for monthly reports
    List<Expense> findByUserAndExpenseDateBetweenOrderByExpenseDateDesc(
            User user, LocalDate start, LocalDate end);

    // THIS IS THE KEY QUERY — total spent in a category for a given month
    // Used for budget breach detection
    // JPQL query: sum all expense amounts for a category in a date range
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
            "WHERE e.category = :category " +
            "AND e.user = :user " +
            "AND e.expenseDate BETWEEN :start AND :end")
    BigDecimal sumByCategoryAndUserAndDateRange(
            @Param("category") Category category,
            @Param("user") User user,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);
}