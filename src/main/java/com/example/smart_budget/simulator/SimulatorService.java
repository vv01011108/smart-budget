package com.example.smart_budget.simulator;

import com.example.smart_budget.FixedExpense.FixedExpense;
import com.example.smart_budget.FixedExpense.FixedExpenseRepository;
import com.example.smart_budget.budget.Budget;
import com.example.smart_budget.budget.BudgetRepository;
import com.example.smart_budget.simulator.dto.SimulationRequest;
import com.example.smart_budget.simulator.dto.SimulationResponse;
import com.example.smart_budget.transaction.Transaction;
import com.example.smart_budget.transaction.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SimulatorService {

    private static final BigDecimal WARNING_THRESHOLD_RATE = new BigDecimal("0.20");

    private final BudgetRepository budgetRepository;
    private final FixedExpenseRepository fixedExpenseRepository;
    private final TransactionRepository transactionRepository;

    public SimulationResponse simulate(SimulationRequest request) {
        String yearMonth = request.yearMonth();
        YearMonth targetMonth = YearMonth.parse(yearMonth);

        // 1. 해당 월 예산 조회
        Budget budget = budgetRepository.findByTargetYearMonth(yearMonth)
                .orElseThrow(() -> new IllegalArgumentException("해당 월의 예산이 등록되지 않았습니다. yearMonth=" + yearMonth));
        BigDecimal monthlyBudget = budget.getAmount();

        // 2. 고정 지출 합계 계산
        BigDecimal totalFixedExpense = fixedExpenseRepository.findAll()
                .stream()
                .map(FixedExpense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. 해당 월 변동 지출 합계 계산
        LocalDate startDate = targetMonth.atDay(1);
        LocalDate endDate = targetMonth.atEndOfMonth();
        BigDecimal totalVariableExpense = transactionRepository
                .findAllByTransactionDateBetweenOrderByTransactionDateDescIdDesc(startDate, endDate)
                .stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. 구매 전·후 남은 예산 계산
        BigDecimal currentRemainingBudget = monthlyBudget
                .subtract(totalFixedExpense)
                .subtract(totalVariableExpense);
        BigDecimal remainingAfterPurchase = currentRemainingBudget
                .subtract(request.plannedAmount());

        // 5. 남은 일수 및 하루 권장 지출액 계산
        int remainingDays = calculateRemainingDays(targetMonth);
        BigDecimal dailyAvailableAfterPurchase = calculateDailyAvailable(
                remainingAfterPurchase, remainingDays
        );

        // 6. 진단 등급 및 메시지 생성
        SimulationStatus status = determineStatus(monthlyBudget, remainingAfterPurchase);
        String message = createMessage(
                status, request.itemName(), remainingAfterPurchase, dailyAvailableAfterPurchase
        );

        return new SimulationResponse(
                yearMonth,
                request.itemName(),
                request.plannedAmount(),
                monthlyBudget,
                totalFixedExpense,
                totalVariableExpense,
                currentRemainingBudget,
                remainingAfterPurchase,
                remainingDays,
                dailyAvailableAfterPurchase,
                status,
                message
        );
    }

    private int calculateRemainingDays(YearMonth targetMonth) {
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);

        if (targetMonth.equals(currentMonth)) {
            return targetMonth.lengthOfMonth() - today.getDayOfMonth() + 1;
        }
        if (targetMonth.isAfter(currentMonth)) {
            return targetMonth.lengthOfMonth();
        }
        return 0;
    }

    private BigDecimal calculateDailyAvailable(BigDecimal remainingAfterPurchase, int remainingDays) {
        if (remainingAfterPurchase.compareTo(BigDecimal.ZERO) <= 0 || remainingDays <= 0) {
            return BigDecimal.ZERO;
        }
        return remainingAfterPurchase.divide(
                BigDecimal.valueOf(remainingDays), 0, RoundingMode.DOWN
        );
    }

    private SimulationStatus determineStatus(BigDecimal monthlyBudget, BigDecimal remainingAfterPurchase) {
        if (remainingAfterPurchase.compareTo(BigDecimal.ZERO) < 0) {
            return SimulationStatus.OVER_BUDGET;
        }
        BigDecimal warningThreshold = monthlyBudget.multiply(WARNING_THRESHOLD_RATE);
        if (remainingAfterPurchase.compareTo(warningThreshold) < 0) {
            return SimulationStatus.WARNING;
        }
        return SimulationStatus.SAFE;
    }

    private String createMessage(
            SimulationStatus status,
            String itemName,
            BigDecimal remainingAfterPurchase,
            BigDecimal dailyAvailable
    ) {
        return switch (status) {
            case OVER_BUDGET -> itemName + "을(를) 구매하면 해당 월 예산을 "
                    + remainingAfterPurchase.abs() + "원 초과합니다.";
            case WARNING -> itemName + "을(를) 구매할 수는 있지만 남은 예산이 월 예산의 20% 미만입니다. "
                    + "남은 기간 하루 권장 지출액은 " + dailyAvailable + "원입니다.";
            case SAFE -> itemName + "을(를) 구매해도 예산이 여유롭습니다! "
                    + "남은 기간 하루 권장 지출액은 " + dailyAvailable + "원입니다.";
        };
    }

}
