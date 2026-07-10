package com.gyanbiswal.expenseflow.controller;

import com.gyanbiswal.expenseflow.dto.request.ExpenseRequest;
import com.gyanbiswal.expenseflow.dto.response.ExpenseResponse;
import com.gyanbiswal.expenseflow.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Track and manage your expenses")
@SecurityRequirement(name = "bearerAuth")
public class ExpenseController {

    private final ExpenseService expenseService;

    @Operation(summary = "Create a new expense")
    @PostMapping
    public ResponseEntity<ExpenseResponse> create(
            @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(expenseService.createExpense(request));
    }

    @Operation(summary = "Get all expenses")
    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getAll() {
        return ResponseEntity.ok(expenseService.getAllExpenses());
    }

    @Operation(summary = "Get expense by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getExpenseById(id));
    }

    @Operation(summary = "Update expense")
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.ok(expenseService.updateExpense(id, request));
    }

    @Operation(summary = "Delete expense")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/v1/expenses/range?start=2026-07-01&end=2026-07-31
    @Operation(summary = "Get expenses by date range")
    @GetMapping("/range")
    public ResponseEntity<List<ExpenseResponse>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(expenseService.getExpensesByDateRange(start, end));
    }

    // GET /api/v1/expenses/paged?page=0&size=10&sort=expenseDate,desc
    @GetMapping("/paged")
    @Operation(summary = "Get expenses with pagination")
    public ResponseEntity<Page<ExpenseResponse>> getAllPaginated(
            @PageableDefault(size = 10, sort = "expenseDate",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(expenseService.getAllExpensesPaginated(pageable));
    }
}