package com.example.smart_budget.simulator.dto;

import com.example.smart_budget.simulator.SimulationStatus;

import java.math.BigDecimal;

public record SimulationResponse(
        String yearMonth,
        String itemName,
        BigDecimal plannedAmount,
        BigDecimal monthlyBudget,
        BigDecimal totalFixedExpense,
        BigDecimal totalVariableExpense,
        BigDecimal currentRemainingBudget,
        BigDecimal remainingAfterPurchase,
        int remainingDays,
        BigDecimal dailyAvailableAfterPurchase,
        SimulationStatus status,
        String message
) {
}
