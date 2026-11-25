package com.example.smart_budget.budget;

import com.example.smart_budget.budget.dto.BudgetRequest;
import com.example.smart_budget.budget.dto.BudgetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BudgetService {

    private final BudgetRepository budgetRepository;

    // 1. 월 예산 등록 또는 수정
    @Transactional
    public BudgetResponse upsertBudget(String targetYearMonth, BudgetRequest request) {
        Optional<Budget> existingBudget = budgetRepository.findByTargetYearMonth(targetYearMonth);

        Budget savedBudget;
        if (existingBudget.isPresent()) {
            savedBudget = existingBudget.get();
            savedBudget.updateAmount(request.amount());
        } else {
            Budget newBudget = new Budget(targetYearMonth, request.amount());
            savedBudget = budgetRepository.save(newBudget);
        }

        return BudgetResponse.from(savedBudget);
    }

    // 2. 월 예산 조회
    public BudgetResponse getBudget(String targetYearMonth) {
        Budget budget = budgetRepository.findByTargetYearMonth(targetYearMonth)
                .orElseThrow(() -> new IllegalArgumentException("해당 월의 예산이 없습니다: " + targetYearMonth));

        return BudgetResponse.from(budget);
    }
}
