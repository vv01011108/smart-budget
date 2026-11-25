package com.example.smart_budget.simulator;

public enum SimulationStatus {
    SAFE,        // 안전 (구매 후 남은 예산이 월 예산의 20% 이상)
    WARNING,     // 안전 (구매 후 남은 예산이 월 예산의 20% 이상)
    OVER_BUDGET  // 초과 (구매 시 월 예산 초과)
}
