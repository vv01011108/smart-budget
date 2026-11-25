package com.example.smart_budget.simulator.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record SimulationRequest(
        @NotBlank(message = "기준 연월은 필수입니다.")
        @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "연월 형식은 YYYY-MM 이어야 합니다.")
        String yearMonth,

        @NotNull(message = "구매 항복 이름은 필수입니다.")
        @Size(max = 100, message = "구매 항목 이름은 100자 이하로 입력해 주세요.")
        String itemName,

        @NotNull(message = "구매 예정 금액은 필수입니다.")
        @Positive(message = "구매 예정 금액은 0보다 커야 합니다.")
        BigDecimal plannedAmount
) {
}
