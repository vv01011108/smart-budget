package com.example.smart_budget.ai.dto;

import java.util.List;

public record GeminiResponse(List<Candidate> candidates) {

    public String extractText() {
        if (candidates == null || candidates.isEmpty()) {
            throw new IllegalStateException("AI 응답 결과가 비어 있습니다.");
        }
        Candidate first = candidates.get(0);
        if (first.content() == null || first.content().parts() == null || first.content().parts().isEmpty()) {
            throw new IllegalStateException("AI 응답 텍스트를 찾을 수 없습니다.");
        }
        return first.content().parts().get(0).text();
    }

    public record Candidate(Content content) {
    }

    public record Content(List<Part> parts) {
    }

    public record Part(String text) {
    }
}
