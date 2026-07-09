package com.gyanbiswal.expenseflow.repository;

import com.gyanbiswal.expenseflow.model.Category;
import com.gyanbiswal.expenseflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // All categories belonging to a specific user
    List<Category> findByUser(User user);

    // Find a specific category by id AND user — prevents accessing other users' categories
    Optional<Category> findByIdAndUser(Long id, User user);

    // Check for duplicate category names per user
    boolean existsByNameAndUser(String name, User user);
}