package com.example.smart_budget.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GeminiRequest(
        List<Content> contents,
        GenerationConfig generationConfig
) {
    public static GeminiRequest of(String prompt) {
        return new GeminiRequest(
                List.of(new Content(List.of(new Part(prompt)))),
                null
        );
    }

    public static GeminiRequest ofJson(String prompt) {
        return new GeminiRequest(
                List.of(new Content(List.of(new Part(prompt)))),
                new GenerationConfig("application/json")
        );
    }

    public record Content(List<Part> parts) {
    }

    public record Part(String text) {
    }

    public record GenerationConfig(String responseMimeType) {
    }
}
