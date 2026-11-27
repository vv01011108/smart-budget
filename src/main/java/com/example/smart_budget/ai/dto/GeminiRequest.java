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
                List.of(new Content(List.of(Part.ofText(prompt)))),
                null
        );
    }

    public static GeminiRequest ofJson(String prompt) {
        return new GeminiRequest(
                List.of(new Content(List.of(Part.ofText(prompt)))),
                new GenerationConfig("application/json")
        );
    }

    public static GeminiRequest ofJsonWithImage(String prompt, String mimeType, String base64Data) {
        return new GeminiRequest(
                List.of(new Content(List.of(
                        Part.ofText(prompt),
                        Part.ofImage(mimeType, base64Data)
                ))),
                new GenerationConfig("application/json")
        );
    }

    public record Content(List<Part> parts) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Part(String text, InlineData inlineData) {
        public static Part ofText(String text) {
            return new Part(text, null);
        }

        public static Part ofImage(String mimeType, String base64Data) {
            return new Part(null, new InlineData(mimeType, base64Data));
        }
    }

    public record InlineData(String mimeType, String data) {

    }

    public record GenerationConfig(String responseMimeType) {
    }
}
