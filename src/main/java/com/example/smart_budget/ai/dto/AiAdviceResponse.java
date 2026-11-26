package com.example.smart_budget.ai.dto;

import com.example.smart_budget.simulator.dto.SimulationResponse;

public record AiAdviceResponse(
        SimulationResponse simulation,
        String aiComment,
        boolean aiGenerated
) {
}
