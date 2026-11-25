package com.example.smart_budget.FixedExpense.dto;

import com.example.smart_budget.FixedExpense.FixedExpense;

import java.math.BigDecimal;

public record FixedExpenseResponse(
        Long id,
        String name,
        BigDecimal amount,
        Integer billingDay
) {
    public static FixedExpenseResponse from(FixedExpense fixedExpense) {
        return new FixedExpenseResponse(
                fixedExpense.getId(),
                fixedExpense.getName(),
                fixedExpense.getAmount(),
                fixedExpense.getBillingDay()
        );
    }
}
