package com.example.smart_budget.FixedExpense;

import com.example.smart_budget.FixedExpense.dto.FixedExpenseRequest;
import com.example.smart_budget.FixedExpense.dto.FixedExpenseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fixed-expenses")
@RequiredArgsConstructor
public class FixedExpenseController {

    private final FixedExpenseService fixedExpenseService;

    // 1. 고정 지출 등록
    @PostMapping
    public ResponseEntity<FixedExpenseResponse> createFixedExpense(
            @Valid @RequestBody FixedExpenseRequest request
    ) {
        FixedExpenseResponse response = fixedExpenseService.createFixedExpense(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. 고정 지출 전체 목록 조회
    @GetMapping
    public ResponseEntity<List<FixedExpenseResponse>> getFixedExpenses() {
        List<FixedExpenseResponse> responses = fixedExpenseService.getFixedExpenses();
        return ResponseEntity.ok(responses);
    }

    // 3. 고정 지출 수정
    @PutMapping("/{id}")
    public ResponseEntity<FixedExpenseResponse> updateFixedExpense(
            @PathVariable Long id,
            @Valid @RequestBody FixedExpenseRequest request
    ) {
        FixedExpenseResponse response = fixedExpenseService.updateFixedExpense(id, request);
        return ResponseEntity.ok(response);
    }

    // 4. 고정 지출 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFixedExpense(@PathVariable Long id) {
        fixedExpenseService.deleteFixedExpense(id);
        return ResponseEntity.noContent().build();
    }

}
