package com.example.smart_budget.ai.dto;

import com.example.smart_budget.transaction.TransactionCategory;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ParsedTransactionResponse(
        String title,
        BigDecimal amount,
        TransactionCategory category,
        LocalDate transactionDate,
        String memo
) {
}
