package com.example.smart_budget.transaction.dto;

import com.example.smart_budget.transaction.TransactionCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest (
        @NotBlank(message = "지출 내역은 필수입니다.")
        @Size(max = 100, message = "지출 내역은 100자 이하로 입력해 주세요.")
        String title,

        @NotNull(message = "금액은 필수입니다.")
        @Positive(message = "금액은 0보다 커야 합니다.")
        BigDecimal amount,

        @NotNull(message = "카테고리는 필수입니다.")
        TransactionCategory category,

        @NotNull(message = "지출 날짜는 필수입니다.")
        LocalDate transactionDate,

        @Size(max = 255, message = "메모는 255자 이하로 입력해 주세요.")
        String memo
){}