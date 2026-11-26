package com.example.smart_budget.ai;

import com.example.smart_budget.ai.dto.AiAdviceResponse;
import com.example.smart_budget.ai.dto.ParsedTransactionResponse;
import com.example.smart_budget.ai.dto.TransactionParseRequest;
import com.example.smart_budget.simulator.dto.SimulationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiAdviceService aiAdviceService;
    private final AiTransactionParseService aiTransactionParseService;

    @PostMapping("/advice")
    public ResponseEntity<AiAdviceResponse> getAdvice(
            @Valid @RequestBody SimulationRequest request
    ) {
        return ResponseEntity.ok(aiAdviceService.getAdvice(request));
    }

    @PostMapping("/parse-transaction")
    public ResponseEntity<ParsedTransactionResponse> parseTransaction(
            @Valid @RequestBody TransactionParseRequest request
    ) {
        return ResponseEntity.ok(aiTransactionParseService.parse(request));
    }
}
