package com.example.smart_budget.FixedExpense;

import com.example.smart_budget.FixedExpense.dto.FixedExpenseRequest;
import com.example.smart_budget.FixedExpense.dto.FixedExpenseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FixedExpenseService {

    private final FixedExpenseRepository fixedExpenseRepository;

    // 1. 고정 지출 등록
    @Transactional
    public FixedExpenseResponse createFixedExpense(FixedExpenseRequest request) {
        FixedExpense fixedExpense = new FixedExpense(
                request.name(),
                request.amount(),
                request.billingDay()
        );
        FixedExpense saved = fixedExpenseRepository.save(fixedExpense);
        return FixedExpenseResponse.from(saved);
    }

    // 2. 고정 지출 전체 목록 조회 (결제일 오름차순)
    @Transactional
    public List<FixedExpenseResponse> getFixedExpenses() {
        return fixedExpenseRepository.findAllByOrderByBillingDayAsc()
                .stream()
                .map(FixedExpenseResponse::from)
                .toList();
    }

    // 3. 고정 지출 수정
    @Transactional
    public FixedExpenseResponse updateFixedExpense(Long id, FixedExpenseRequest request) {
        FixedExpense fixedExpense = fixedExpenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 고정지출입니다. id=" + id));

        fixedExpense.update(request.name(), request.amount(), request.billingDay());
        return FixedExpenseResponse.from(fixedExpense);
    }

    // 4. 고정지출 삭제
    @Transactional
    public void deleteFixedExpense(Long id) {
        FixedExpense fixedExpense = fixedExpenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 고정지출입니다. id=" + id));

        fixedExpenseRepository.delete(fixedExpense);
    }

}
