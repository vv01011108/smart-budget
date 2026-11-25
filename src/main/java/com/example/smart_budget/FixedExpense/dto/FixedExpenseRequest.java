package com.example.smart_budget.FixedExpense.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record FixedExpenseRequest (
        @NotBlank(message = "항목 이름은 필수입니다.")
        @Size(max = 100, message = "항목 이름은 100자 이하로 입력해 주세요.")
        String name,

        @NotNull(message = "금액은 필수입니다.")
        @Positive(message = "금액은 0보다 커야 합니다.")
        BigDecimal amount,

        @NotNull(message = "결제일은 필수입니다.")
        @Min(value = 1, message = "결제일은 1일 이상이어야 합니다.")
        @Max(value = 31, message = "결제일은 31일 이하여야 합니다.")
        Integer billingDay
){}
