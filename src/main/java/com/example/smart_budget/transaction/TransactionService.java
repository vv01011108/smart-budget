package com.example.smart_budget.transaction;

import com.example.smart_budget.transaction.dto.TransactionRequest;
import com.example.smart_budget.transaction.dto.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;


    // 1. 변동 지출 등록
    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        Transaction transaction = new Transaction(
                request.title(),
                request.amount(),
                request.category(),
                request.transactionDate(),
                request.memo()
        );
        Transaction saved = transactionRepository.save(transaction);
        return TransactionResponse.from(saved);
    }

    // 2. 변동 지출 목록 조회 (전체 또는 특정 월별)
    public List<TransactionResponse> getTransactions(String yearMonth) {
        List<Transaction> transactions;

        if (yearMonth == null || yearMonth.isBlank()) {
            transactions = transactionRepository.findAllByOrderByTransactionDateDescIdDesc();
        } else {
            YearMonth target = YearMonth.parse(yearMonth);
            LocalDate startDate = target.atDay(1);
            LocalDate endDate = target.atEndOfMonth();
            transactions = transactionRepository.findAllByTransactionDateBetweenOrderByTransactionDateDescIdDesc(startDate, endDate);
        }

        return transactions.stream()
                .map(TransactionResponse::from)
                .toList();
    }

    // 3. 변동 지출 수정
    @Transactional
    public TransactionResponse updateTransaction(Long id, TransactionRequest request) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지출 내역입니다. id=" + id));

        transaction.update(
                request.title(),
                request.amount(),
                request.category(),
                request.transactionDate(),
                request.memo()
        );
        return TransactionResponse.from(transaction);
    }

    // 4. 변동 지출 삭제
    @Transactional
    public void deleteTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지출 내역입니다. id=" + id));

        transactionRepository.delete(transaction);
    }
}
