package com.example.smart_budget.budget.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record BudgetRequest (
        @NotNull(message = "예산 금액은 필수입니다")
        @Positive(message = "에산 금액은 0보다 커야 합니다")
        BigDecimal amount
) {}


