// 정상 응답 & 장애 시 Fallback 검증

package com.example.smart_budget.ai;

import com.example.smart_budget.ai.dto.AiAdviceResponse;
import com.example.smart_budget.simulator.SimulatorService;
import com.example.smart_budget.simulator.dto.SimulationRequest;
import com.example.smart_budget.simulator.dto.SimulationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AiAdviceServiceTest {

    @Mock
    private SimulatorService simulatorService;

    @Mock
    private GeminiClient geminiClient;

    @InjectMocks
    private AiAdviceService aiAdviceService;

    @Test
    @DisplayName("Gemini API 호출 성공 시 AI 코멘트와 aiGenerated=true를 반환한다")
    void getAdvice_success() {
        // given
        SimulationRequest request = new SimulationRequest("2026-11", "겨울 코트", BigDecimal.valueOf(300000));
        SimulationResponse mockSimulation = mock(SimulationResponse.class);

        when(simulatorService.simulate(request)).thenReturn(mockSimulation);
        when(geminiClient.generateText(anyString())).thenReturn("  충분히 구매 가능한 예산입니다.  ");

        // when
        AiAdviceResponse response = aiAdviceService.getAdvice(request);

        // then
        assertThat(response.aiGenerated()).isTrue();
        assertThat(response.aiComment()).isEqualTo("충분히 구매 가능한 예산입니다.");
        assertThat(response.simulation()).isSameAs(mockSimulation);
    }

    @Test
    @DisplayName("Gemini API 장애 발생 시 예외 대신 기본 시뮬레이션 메시지로 Fallback한다")
    void getAdvice_fallbackWhenApiFails() {
        // given
        SimulationRequest request = new SimulationRequest("2026-11", "겨울 코트", BigDecimal.valueOf(300000));
        SimulationResponse mockSimulation = mock(SimulationResponse.class);

        when(mockSimulation.message()).thenReturn("기본 시뮬레이션 진단 메시지");
        when(simulatorService.simulate(request)).thenReturn(mockSimulation);
        when(geminiClient.generateText(anyString()))
                .thenThrow(new IllegalStateException("AI API 타임아웃"));

        // when
        AiAdviceResponse response = aiAdviceService.getAdvice(request);

        // then
        assertThat(response.aiGenerated()).isFalse();
        assertThat(response.aiComment()).isEqualTo("기본 시뮬레이션 진단 메시지");
    }
}
