package com.example.smart_budget.ai;

import com.example.smart_budget.ai.dto.ParsedTransactionResponse;
import com.example.smart_budget.ai.dto.TransactionParseRequest;
import com.example.smart_budget.transaction.TransactionCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiTransactionParseService {

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    public ParsedTransactionResponse parse(TransactionParseRequest request) {
        String prompt = buildPrompt(request.rawText());
        String jsonText = geminiClient.generateJson(prompt);

        try {
            ParsedTransactionResponse parsed = objectMapper.readValue(
                    cleanJson(jsonText),
                    ParsedTransactionResponse.class
            );

            if (parsed.amount() == null || parsed.amount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("결제 문자에서 유효한 금액을 찾을 수 없습니다.");
            }
            return parsed;
        } catch (JacksonException e) {
            log.warn("결제 문자 JSON 파싱 실패. rawResponse={}", jsonText, e);
            throw new IllegalArgumentException("결제 문자를 분석할 수 없습니다. 금액과 결제처가 포함된 문자를 입력해 주세요.");
        }
    }

    private String buildPrompt(String rawText) {
        String categories = Arrays.stream(TransactionCategory.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
        LocalDate today = LocalDate.now();

        return """
                너는 한국의 카드 승인 문자 및 결제 알림 텍스트에서 가계부 지출 정보를 추출하는 파서야.
                아래 결제 문자를 분석해서 반드시 지정된 JSON 형식으로만 응답해 줘.

                [오늘 날짜]
                %s

                [허용된 category 목록 (반드시 이 중 하나만 선택)]
                %s

                [응답 JSON 스키마]
                {
                  "title": "가맹점명 또는 지출 내용 (짧고 깔끔하게)",
                  "amount": 0,
                  "category": "허용된 category 중 하나",
                  "transactionDate": "YYYY-MM-DD",
                  "memo": "할부 여부, 카드사명 등 부가 정보 (없으면 빈 문자열)"
                }

                [규칙]
                1. amount는 쉼표나 '원' 없이 숫자만 넣어 줘. 금액을 찾을 수 없으면 0으로 넣어 줘.
                2. 날짜에 연도가 생략되어 있으면 오늘 날짜의 연도를 사용하고, 날짜가 아예 없으면 오늘 날짜(%s)를 사용해 줘.
                3. category는 반드시 허용된 목록 중 가장 알맞은 값 하나를 대문자 그대로 선택해 줘.

                [결제 문자]
                %s
                """.formatted(today, categories, today, rawText);
    }

    private String cleanJson(String raw) {
        return raw.trim()
                .replaceAll("^```json\\s*", "")
                .replaceAll("^```\\s*", "")
                .replaceAll("\\s*```$", "")
                .trim();
    }
}
