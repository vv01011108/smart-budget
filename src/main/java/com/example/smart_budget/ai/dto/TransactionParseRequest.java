package com.example.smart_budget.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TransactionParseRequest(
        @NotBlank(message = "분석할 결제 문자 내용을 입력해 주세요.")
        @Size(max = 500, message = "결제 문자는 500자 이하로 입력해 주세요.")
        String rawText
) {
}
