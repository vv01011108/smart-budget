package com.example.smart_budget.simulator;

import com.example.smart_budget.FixedExpense.FixedExpense;
import com.example.smart_budget.FixedExpense.FixedExpenseRepository;
import com.example.smart_budget.budget.Budget;
import com.example.smart_budget.budget.BudgetRepository;
import com.example.smart_budget.simulator.dto.SimulationRequest;
import com.example.smart_budget.simulator.dto.SimulationResponse;
import com.example.smart_budget.transaction.Transaction;
import com.example.smart_budget.transaction.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimulatorServiceTest {

    private static final String TARGET_YEAR_MONTH = "2099-04"; // 30일 고정 (LocalDate.now() 영향 제거)
    private static final LocalDate START_DATE = LocalDate.of(2099, 4, 1);
    private static final LocalDate END_DATE = LocalDate.of(2099, 4, 30);

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private FixedExpenseRepository fixedExpenseRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private SimulatorService simulatorService;

    @Test
    @DisplayName("구매 후 남은 예산이 월 예산의 20% 이상이면 SAFE와 하루 권장 지출액을 반환한다")
    void simulate_safe() {
        // given: 월 예산 100만원, 고정 20만원, 변동 10만원 -> 현재 잔액 70만원
        mockBudgetAndExpenses(1_000_000, 200_000, 100_000);
        SimulationRequest request = new SimulationRequest(TARGET_YEAR_MONTH, "운동화", BigDecimal.valueOf(100_000));

        // when
        SimulationResponse response = simulatorService.simulate(request);

        // then: 구매 후 잔액 60만원 (>= 20만원), 30일 기준 하루 2만원
        assertThat(response.status()).isEqualTo(SimulationStatus.SAFE);
        assertThat(response.currentRemainingBudget()).isEqualByComparingTo("700000");
        assertThat(response.remainingAfterPurchase()).isEqualByComparingTo("600000");
        assertThat(response.remainingDays()).isEqualTo(30);
        assertThat(response.dailyAvailableAfterPurchase()).isEqualByComparingTo("20000");
    }

    @Test
    @DisplayName("구매 후 남은 예산이 0 이상이고 월 예산의 20% 미만이면 WARNING을 반환한다")
    void simulate_warning() {
        // given: 월 예산 100만원, 고정 50만원, 변동 30만원 -> 현재 잔액 20만원
        mockBudgetAndExpenses(1_000_000, 500_000, 300_000);
        SimulationRequest request = new SimulationRequest(TARGET_YEAR_MONTH, "향수", BigDecimal.valueOf(50_000));

        // when
        SimulationResponse response = simulatorService.simulate(request);

        // then: 구매 후 잔액 15만원 (< 20만원), 30일 기준 하루 5천원
        assertThat(response.status()).isEqualTo(SimulationStatus.WARNING);
        assertThat(response.remainingAfterPurchase()).isEqualByComparingTo("150000");
        assertThat(response.dailyAvailableAfterPurchase()).isEqualByComparingTo("5000");
        assertThat(response.message()).contains("20% 미만");
    }

    @Test
    @DisplayName("구매 후 남은 예산이 음수이면 OVER_BUDGET과 하루 권장 지출액 0원을 반환한다")
    void simulate_overBudget() {
        // given: 월 예산 100만원, 고정 60만원, 변동 30만원 -> 현재 잔액 10만원
        mockBudgetAndExpenses(1_000_000, 600_000, 300_000);
        SimulationRequest request = new SimulationRequest(TARGET_YEAR_MONTH, "겨울 코트", BigDecimal.valueOf(250_000));

        // when
        SimulationResponse response = simulatorService.simulate(request);

        // then: 구매 후 잔액 -15만원, 하루 권장 지출액 0원
        assertThat(response.status()).isEqualTo(SimulationStatus.OVER_BUDGET);
        assertThat(response.remainingAfterPurchase()).isEqualByComparingTo("-150000");
        assertThat(response.dailyAvailableAfterPurchase()).isEqualByComparingTo("0");
        assertThat(response.message()).contains("150000원 초과");
    }

    @Test
    @DisplayName("해당 월 예산이 등록되지 않았으면 IllegalArgumentException이 발생한다")
    void simulate_failWhenBudgetNotFound() {
        // given
        when(budgetRepository.findByTargetYearMonth(TARGET_YEAR_MONTH)).thenReturn(Optional.empty());
        SimulationRequest request = new SimulationRequest(TARGET_YEAR_MONTH, "겨울 코트", BigDecimal.valueOf(100_000));

        // when & then
        assertThatThrownBy(() -> simulatorService.simulate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("예산이 등록되지 않았습니다");
    }

    private void mockBudgetAndExpenses(long budgetAmount, long fixedAmount, long variableAmount) {
        Budget budget = mock(Budget.class);
        when(budget.getAmount()).thenReturn(BigDecimal.valueOf(budgetAmount));
        when(budgetRepository.findByTargetYearMonth(TARGET_YEAR_MONTH)).thenReturn(Optional.of(budget));

        FixedExpense fixed = mock(FixedExpense.class);
        when(fixed.getAmount()).thenReturn(BigDecimal.valueOf(fixedAmount));
        when(fixedExpenseRepository.findAll()).thenReturn(List.of(fixed));

        Transaction tx = mock(Transaction.class);
        when(tx.getAmount()).thenReturn(BigDecimal.valueOf(variableAmount));
        when(transactionRepository.findAllByTransactionDateBetweenOrderByTransactionDateDescIdDesc(START_DATE, END_DATE))
                .thenReturn(List.of(tx));
    }
}
