package com.aiagent.orchestrator.controller;

import com.aiagent.common.dto.OrchestratorTaskRequest;
import com.aiagent.common.dto.OrchestratorTaskResponse;
import com.aiagent.orchestrator.service.MinimalOrchestratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orchestrator")
public class MinimalOrchestratorController {

    @Autowired
    private MinimalOrchestratorService minimalOrchestratorService;

    @PostMapping("/execute")
    public ResponseEntity<OrchestratorTaskResponse> execute(@RequestBody OrchestratorTaskRequest request) {
        OrchestratorTaskResponse response = minimalOrchestratorService.execute(request);
        return ResponseEntity.ok(response);
    }
}