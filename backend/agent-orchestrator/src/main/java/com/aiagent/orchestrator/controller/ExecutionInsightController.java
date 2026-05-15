package com.aiagent.orchestrator.controller;

import com.aiagent.common.model.ExecutionInsight;
import com.aiagent.orchestrator.service.ExecutionInsightService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orchestrator/insights")
public class ExecutionInsightController {
    private final ExecutionInsightService executionInsightService;

    public ExecutionInsightController(ExecutionInsightService executionInsightService) {
        this.executionInsightService = executionInsightService;
    }

    @GetMapping
    public ResponseEntity<List<ExecutionInsight>> getInsights(
            @RequestParam(required = false) Integer limit) {
        List<ExecutionInsight> insights = executionInsightService.analyze(limit);
        return ResponseEntity.ok(insights);
    }
}
