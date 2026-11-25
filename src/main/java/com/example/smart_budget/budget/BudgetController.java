package com.example.smart_budget.budget;

import com.example.smart_budget.budget.dto.BudgetRequest;
import com.example.smart_budget.budget.dto.BudgetResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    // 1. 월 예산 등록 또는 수정
    @PutMapping("/{yearMonth}")
    public ResponseEntity<BudgetResponse> upsertBudget(
            @PathVariable String yearMonth,
            @Valid @RequestBody BudgetRequest request
    ){
        BudgetResponse response = budgetService.upsertBudget(yearMonth, request);
        return ResponseEntity.ok(response);
    }

    // 2. 월 예산 조회
    @GetMapping("/{yearMonth}")
    public ResponseEntity<BudgetResponse> getBudget(@PathVariable String yearMonth){
        BudgetResponse response = budgetService.getBudget(yearMonth);
        return ResponseEntity.ok(response);
    }
}
