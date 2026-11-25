package com.example.smart_budget.transaction;

import com.example.smart_budget.transaction.dto.TransactionRequest;
import com.example.smart_budget.transaction.dto.TransactionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    // 1. 변동 지출 목록
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody TransactionRequest request
    ) {
      TransactionResponse response = transactionService.createTransaction(request);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. 변동 지출 목록 조회 (전체 또는 특정 월별)
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getTransactions(
            @RequestParam(required = false) String yearMonth
    ) {
        List<TransactionResponse> responses = transactionService.getTransactions(yearMonth);
        return ResponseEntity.ok(responses);
    }

    // 3. 변동 지출 수정
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request
    ) {
        TransactionResponse response = transactionService.updateTransaction(id, request);
        return ResponseEntity.ok(response);
    }

    // 4. 변동 지출 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
}
