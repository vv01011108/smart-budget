package com.example.smart_budget.ai;

import com.example.smart_budget.ai.dto.AiAdviceResponse;
import com.example.smart_budget.simulator.SimulatorService;
import com.example.smart_budget.simulator.dto.SimulationRequest;
import com.example.smart_budget.simulator.dto.SimulationResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAdviceService {

    private final SimulatorService simulatorService;
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiAdviceResponse getAdvice(SimulationRequest request) {
        // 1. 기존 시뮬레이터로 정확한 예산 수치 먼저 계산
        SimulationResponse simulation = simulatorService.simulate(request);

        // 2. 계산 결과를 바탕으로 Gemini 프롬프트 구성 및 호출
        String prompt = buildPrompt(simulation);

        try {
            String rawComment = geminiClient.generateText(prompt);
            String aiComment = extractAdviceText(rawComment);
            return new AiAdviceResponse(simulation, aiComment, true);
        } catch (IllegalStateException e) {
            // 3. 외부 AI API 장애·타임아웃 시 서버 에러 대신 기본 메시지로 대체(FallBack)
            log.warn("Gemini AI 코멘트 생성 실패 - 기본 메시지로 대체합니다. reason={}", e.getMessage());
            return new AiAdviceResponse(simulation, simulation.message(), false);
        }
    }

    private String extractAdviceText(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return "";
        }

        String trimmed = rawText.trim();

        // ```json ... ``` 또는 ``` ... ``` 형태 제거
        if (trimmed.startsWith("```")) {
            trimmed = trimmed
                    .replaceFirst("^```(?:json)?\\s*", "")
                    .replaceFirst("\\s*```$", "")
                    .trim();
        }

        // JSON 객체 형태라면 "advice" 필드만 추출
        if (trimmed.startsWith("{")) {
            try {
                JsonNode root = objectMapper.readTree(trimmed);
                if (root.hasNonNull("advice")) {
                    return root.get("advice").asText().trim();
                }
            } catch (Exception ignored) {
                // JSON 파싱 실패 시 원본 문자열 유지
            }
        }

        return trimmed;
    }

    private String buildPrompt(SimulationResponse s) {
        return """
                너는 친절하지만 현실적인 개인 재무 코치야.
                아래 예산 시뮬레이션 결과를 보고 사용자가 이 소비를 결정하는 데 도움이 되도록
                한국어로 2~3문장 이내의 짧고 구체적인 조언을 작성해 줘.
                JSON이나 마크다운 기호(*, # 등), 인사말 없이 순수 조언 문장만 출력해 줘.

- 기준 연월: %s
- 구매 예정 항목: %s
- 구매 예정 금액: %s원
- 월 예산: %s원
- 고정 지출 합계: %s원
- 이번 달 변동 지출 합계: %s원
- 구매 전 남은 예산: %s원
- 구매 후 남은 예산: %s원
- 이번 달 남은 일수: %d일
- 구매 후 하루 권장 지출액: %s원
- 진단 등급: %s
                """.formatted(
                s.yearMonth(),
                s.itemName(),
                s.plannedAmount(),
                s.monthlyBudget(),
                s.totalFixedExpense(),
                s.totalVariableExpense(),
                s.currentRemainingBudget(),
                s.remainingAfterPurchase(),
                s.remainingDays(),
                s.dailyAvailableAfterPurchase(),
                s.status()
        );
    }
}