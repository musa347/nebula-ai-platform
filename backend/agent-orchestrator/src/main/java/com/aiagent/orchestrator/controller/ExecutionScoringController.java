package com.aiagent.orchestrator.controller;

import com.aiagent.common.model.ExecutionScore;
import com.aiagent.orchestrator.service.ExecutionScoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orchestrator/score")
public class ExecutionScoringController {
    private final ExecutionScoringService executionScoringService;

    public ExecutionScoringController(ExecutionScoringService executionScoringService) {
        this.executionScoringService = executionScoringService;
    }

    @GetMapping("/{executionId}")
    public ResponseEntity<ExecutionScore> score(@PathVariable String executionId) {
        ExecutionScore score = executionScoringService.score(executionId);
        return ResponseEntity.ok(score);
    }
}
