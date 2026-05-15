package com.aiagent.orchestrator.controller;

import com.aiagent.common.model.ComparisonResult;
import com.aiagent.orchestrator.service.ExecutionComparisonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orchestrator/compare")
public class ExecutionComparisonController {
    private final ExecutionComparisonService executionComparisonService;

    public ExecutionComparisonController(ExecutionComparisonService executionComparisonService) {
        this.executionComparisonService = executionComparisonService;
    }

    @GetMapping("/{executionA}/{executionB}")
    public ResponseEntity<ComparisonResult> compare(
            @PathVariable String executionA,
            @PathVariable String executionB) {
        ComparisonResult result = executionComparisonService.compare(executionA, executionB);
        return ResponseEntity.ok(result);
    }
}
