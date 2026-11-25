package com.example.smart_budget.transaction.dto;

import com.example.smart_budget.transaction.Transaction;
import com.example.smart_budget.transaction.TransactionCategory;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponse (
        Long id,
        String title,
        BigDecimal amount,
        TransactionCategory category,
        LocalDate transactionDate,
        String memo
){
    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getTitle(),
                transaction.getAmount(),
                transaction.getCategory(),
                transaction.getTransactionDate(),
                transaction.getMemo()
        );
    }
}
