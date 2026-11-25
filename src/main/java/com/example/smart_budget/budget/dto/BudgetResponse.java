package com.example.smart_budget.budget.dto;

import com.example.smart_budget.budget.Budget;

import java.math.BigDecimal;

public record BudgetResponse (
        Long id,
        String targetYearMonth,
        BigDecimal amount
) {
    public static BudgetResponse from(Budget budget) {
        return new BudgetResponse(
                budget.getId(),
                budget.getTargetYearMonth(),
                budget.getAmount()
        );
    }
}
