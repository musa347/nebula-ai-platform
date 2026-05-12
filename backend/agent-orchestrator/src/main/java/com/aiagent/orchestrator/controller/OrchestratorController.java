package com.aiagent.orchestrator.controller;

import com.aiagent.common.dto.ExecutionEvent;
import com.aiagent.orchestrator.retry.RetryOrchestrator;
import com.aiagent.orchestrator.retry.RetryPolicy;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/orchestrate")
public class OrchestratorController {

    private final RetryOrchestrator retryOrchestrator;

    public OrchestratorController(RetryOrchestrator retryOrchestrator) {
        this.retryOrchestrator = retryOrchestrator;
    }

    @PostMapping(value = "/task/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ExecutionEvent> streamTask(@RequestBody TaskRequest request) {
        RetryPolicy policy = new RetryPolicy.Builder()
                .maxRetries(request.getMaxRetries() != null ? request.getMaxRetries() : 3)
                .build();
        return retryOrchestrator.executeWithRetry(request.getTask(), policy);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> status() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "agent-orchestrator"));
    }
}
