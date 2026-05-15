package com.aiagent.orchestrator.controller;

import com.aiagent.common.model.ReplayEvent;
import com.aiagent.orchestrator.service.ExecutionReplayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orchestrator/replay")
public class ExecutionReplayController {
    private final ExecutionReplayService executionReplayService;

    public ExecutionReplayController(ExecutionReplayService executionReplayService) {
        this.executionReplayService = executionReplayService;
    }

    @GetMapping("/{executionId}")
    public ResponseEntity<List<ReplayEvent>> replay(@PathVariable String executionId) {
        List<ReplayEvent> events = executionReplayService.replay(executionId);
        return ResponseEntity.ok(events);
    }
}
