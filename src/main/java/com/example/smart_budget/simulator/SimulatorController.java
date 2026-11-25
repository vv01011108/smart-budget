package com.example.smart_budget.simulator;

import com.example.smart_budget.simulator.dto.SimulationRequest;
import com.example.smart_budget.simulator.dto.SimulationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/simulator")
@RequiredArgsConstructor
public class SimulatorController {

    private final SimulatorService simulatorService;

    @PostMapping
    public ResponseEntity<SimulationResponse> simulate(
            @Valid @RequestBody SimulationRequest request
    ) {
        SimulationResponse response = simulatorService.simulate(request);
        return ResponseEntity.ok(response);
    }
}
