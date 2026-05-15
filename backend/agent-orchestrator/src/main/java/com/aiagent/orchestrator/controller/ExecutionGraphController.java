package com.aiagent.orchestrator.controller;

import com.aiagent.common.model.ExecutionGraph;
import com.aiagent.orchestrator.service.ExecutionGraphService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/orchestrator/graph")
public class ExecutionGraphController {
    private final ExecutionGraphService executionGraphService;

    public ExecutionGraphController(ExecutionGraphService executionGraphService) {
        this.executionGraphService = executionGraphService;
    }

    @GetMapping("/{executionId}")
    public ResponseEntity<ExecutionGraph> getGraph(@PathVariable String executionId) {
        ExecutionGraph graph = executionGraphService.get(executionId);
        if (graph == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(graph);
    }
    
    @PostMapping("/node")
    public ResponseEntity<Void> addNode(@RequestBody Map<String, String> request) {
        String executionId = request.get("executionId");
        String type = request.get("type");
        String message = request.get("message");
        
        if (executionId != null && type != null && message != null) {
            ExecutionGraph graph = executionGraphService.get(executionId);
            if (graph == null) {
                executionGraphService.create(executionId);
            }
            executionGraphService.addNode(executionId, type, message);
        }
        return ResponseEntity.ok().build();
    }
}
