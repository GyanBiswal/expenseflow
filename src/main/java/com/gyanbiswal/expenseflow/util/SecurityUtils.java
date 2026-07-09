package com.gyanbiswal.expenseflow.util;

import com.gyanbiswal.expenseflow.exception.ResourceNotFoundException;
import com.gyanbiswal.expenseflow.model.User;
import com.gyanbiswal.expenseflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    // Reads the email from the JWT (already validated by JwtAuthFilter)
    // Loads and returns the full User entity from the database
    // Called at the start of every secured service method
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found: " + email));
    }
}