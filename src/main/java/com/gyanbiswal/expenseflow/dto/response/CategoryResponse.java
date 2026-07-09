package com.gyanbiswal.expenseflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private BigDecimal budgetLimit;
    // These two come from the aggregation logic — not stored in DB
    private BigDecimal spentThisMonth;
    private boolean budgetExceeded;
}