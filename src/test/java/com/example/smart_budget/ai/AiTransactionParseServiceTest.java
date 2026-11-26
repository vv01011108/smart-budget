// 정상 파싱 · 코드블록 제거 · 금액 누락/깨진 JSON 예외 검증

package com.example.smart_budget.ai;

import com.example.smart_budget.ai.dto.ParsedTransactionResponse;
import com.example.smart_budget.ai.dto.TransactionParseRequest;
import com.example.smart_budget.transaction.TransactionCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiTransactionParseServiceTest {

    @Mock
    private GeminiClient geminiClient;

    private AiTransactionParseService parseService;

    @BeforeEach
    void setUp() {
        // Spring Boot 4 기본 Jackson 3 ObjectMapper 사용
        parseService = new AiTransactionParseService(geminiClient, new ObjectMapper());
    }

    @Test
    @DisplayName("마크다운 코드블록이 포함된 JSON 응답도 정상적으로 파싱한다")
    void parse_successWithCodeFence() {
        // given
        TransactionCategory expectedCategory = TransactionCategory.values()[0];
        String mockJson = """
                ```json
                {
                  "title": "스타벅스 강남역점",
                  "amount": 6500,
                  "category": "%s",
                  "transactionDate": "2026-11-26",
                  "memo": "신한카드 일시불"
                }
                ```
                """.formatted(expectedCategory.name());

        when(geminiClient.generateJson(anyString())).thenReturn(mockJson);

        // when
        ParsedTransactionResponse result = parseService.parse(
                new TransactionParseRequest("[Web발신] 신한카드 승인 스타벅스 6,500원")
        );

        // then
        assertThat(result.title()).isEqualTo("스타벅스 강남역점");
        assertThat(result.amount()).isEqualByComparingTo(BigDecimal.valueOf(6500));
        assertThat(result.category()).isEqualTo(expectedCategory);
        assertThat(result.transactionDate()).isEqualTo(LocalDate.of(2026, 11, 26));
        assertThat(result.memo()).isEqualTo("신한카드 일시불");
    }

    @Test
    @DisplayName("추출된 금액이 0 이하이면 IllegalArgumentException이 발생한다")
    void parse_failWhenAmountIsZero() {
        // given
        TransactionCategory category = TransactionCategory.values()[0];
        String mockJson = """
                {
                  "title": "알 수 없음",
                  "amount": 0,
                  "category": "%s",
                  "transactionDate": "2026-11-26",
                  "memo": ""
                }
                """.formatted(category.name());

        when(geminiClient.generateJson(anyString())).thenReturn(mockJson);

        // when & then
        assertThatThrownBy(() -> parseService.parse(new TransactionParseRequest("오늘 날씨 좋네요")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효한 금액");
    }

    @Test
    @DisplayName("AI 응답이 올바른 JSON 형식이 아니면 IllegalArgumentException이 발생한다")
    void parse_failWhenInvalidJson() {
        // given
        when(geminiClient.generateJson(anyString())).thenReturn("죄송합니다. 분석할 수 없습니다.");

        // when & then
        assertThatThrownBy(() -> parseService.parse(new TransactionParseRequest("이상한 텍스트")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("결제 문자를 분석할 수 없습니다");
    }
}
