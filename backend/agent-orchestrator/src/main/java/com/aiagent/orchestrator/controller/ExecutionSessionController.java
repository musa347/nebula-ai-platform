package com.aiagent.orchestrator.controller;

import com.aiagent.common.dto.StateUpdateRequest;
import com.aiagent.common.model.ExecutionSession;
import com.aiagent.common.model.TransitionResult;
import com.aiagent.orchestrator.service.ExecutionSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orchestrator/session")
public class ExecutionSessionController {

    @Autowired
    private ExecutionSessionService executionSessionService;

    @PostMapping
    public ResponseEntity<ExecutionSession> createSession() {
        ExecutionSession session = executionSessionService.create();
        return ResponseEntity.ok(session);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExecutionSession> getSession(@PathVariable String id) {
        ExecutionSession session = executionSessionService.get(id);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(session);
    }

    @PostMapping("/{id}/state")
    public ResponseEntity<TransitionResult> updateState(
            @PathVariable String id, 
            @RequestBody StateUpdateRequest request) {
        TransitionResult result = executionSessionService.updateState(id, request.getState());
        return ResponseEntity.ok(result);
    }
}