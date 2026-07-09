package com.gyanbiswal.expenseflow.repository;

import com.gyanbiswal.expenseflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data generates the SQL from the method name automatically:
    // SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);

    // Used during registration to prevent duplicate accounts
    boolean existsByEmail(String email);
}