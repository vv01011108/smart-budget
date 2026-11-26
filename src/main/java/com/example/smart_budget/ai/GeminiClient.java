package com.example.smart_budget.ai;

import com.example.smart_budget.ai.dto.GeminiRequest;
import com.example.smart_budget.ai.dto.GeminiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

@Component
public class GeminiClient {

    private final RestClient restClient;
    private final String apiKey;
    private final String apiUrl;

    public GeminiClient(
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.api.url}") String apiUrl
    ) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(15));

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .build();
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
    }

    public String generateText(String prompt) {
        return callGemini(GeminiRequest.ofJson(prompt));
    }

    public String callGemini(GeminiRequest request) {
        try {
            GeminiResponse response = restClient.post()
                    .uri(apiUrl)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(GeminiResponse.class);

            if (response == null) {
                throw new IllegalStateException("AI 서버로부터 빈 응답을 받았습니다.");
            }
            return response.extractText();
        } catch (RestClientException e) {
            throw new IllegalStateException("AI API 호출에 실패했습니다: " + e.getMessage(), e);
        }
    }
}
