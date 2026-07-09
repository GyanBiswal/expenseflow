package com.gyanbiswal.expenseflow.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")  // "user" is a reserved word in PostgreSQL — always use "users"
@Data                   // Lombok: generates getters, setters, equals, hashCode, toString
@Builder                // Lombok: enables User.builder().email("x").build() pattern
@NoArgsConstructor      // Lombok: generates no-arg constructor (required by JPA)
@AllArgsConstructor     // Lombok: generates all-args constructor (required by @Builder)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // IDENTITY = database handles ID generation (PostgreSQL SERIAL / auto-increment)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;   // stored as bcrypt hash — never plaintext

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    // STRING = stores "USER" or "ADMIN" in DB, not 0 or 1
    // Always use STRING — ordinal breaks if you reorder the enum
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    // Runs automatically before the entity is first saved to the database
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}