package com.gyanbiswal.expenseflow.service;

import com.gyanbiswal.expenseflow.dto.request.LoginRequest;
import com.gyanbiswal.expenseflow.dto.request.RegisterRequest;
import com.gyanbiswal.expenseflow.dto.response.AuthResponse;
import com.gyanbiswal.expenseflow.exception.EmailAlreadyExistsException;
import com.gyanbiswal.expenseflow.model.Role;
import com.gyanbiswal.expenseflow.model.User;
import com.gyanbiswal.expenseflow.repository.UserRepository;
import com.gyanbiswal.expenseflow.security.CustomUserDetailsService;
import com.gyanbiswal.expenseflow.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    public AuthResponse register(RegisterRequest request) {

        // Check duplicate email before attempting to save
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "Email already registered: " + request.getEmail()
            );
        }

        // Build and save the user entity
        // Password is hashed with bcrypt — never stored as plaintext
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)  // all self-registered users get USER role
                .build();

        userRepository.save(user);

        // Load as UserDetails and generate token
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        return new AuthResponse(token, user.getEmail(), user.getName(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {

        // AuthenticationManager verifies email + password against the database
        // Throws BadCredentialsException automatically if wrong — we do not handle it manually
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // If we reach here, credentials are valid
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        return new AuthResponse(token, user.getEmail(), user.getName(), user.getRole().name());
    }
}