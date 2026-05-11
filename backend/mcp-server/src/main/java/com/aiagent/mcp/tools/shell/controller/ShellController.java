package com.aiagent.mcp.tools.shell.controller;

import com.aiagent.common.dto.ExecutionEvent;
import com.aiagent.common.dto.ShellExecuteRequest;
import com.aiagent.common.dto.ShellExecuteResponse;
import com.aiagent.mcp.tools.shell.service.ExecutionStreamingService;
import com.aiagent.mcp.tools.shell.service.ShellExecutionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * REST controller for shell command execution.
 * Provides both synchronous and real-time streaming endpoints.
 */
@RestController
@RequestMapping("/api/tools/shell")
public class ShellController {

    private final ShellExecutionService shellExecutionService;
    private final ExecutionStreamingService executionStreamingService;

    public ShellController(ShellExecutionService shellExecutionService, 
                          ExecutionStreamingService executionStreamingService) {
        this.shellExecutionService = shellExecutionService;
        this.executionStreamingService = executionStreamingService;
    }

    @PostMapping("/execute")
    public ResponseEntity<ShellExecuteResponse> execute(@RequestBody ShellExecuteRequest request) {
        try {
            ShellExecuteResponse response = shellExecutionService.execute(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ShellExecuteResponse errorResponse = ShellExecuteResponse.builder()
                    .success(false)
                    .exitCode(-1)
                    .stderr("Internal server error: " + e.getMessage())
                    .durationMs(0L)
                    .build();
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping(value = "/execute/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ExecutionEvent> executeWithStreaming(@RequestBody ShellExecuteRequest request) {
        return executionStreamingService.executeWithLiveStreaming(request.getCommand(), request.getWorkingDirectory());
    }

    @GetMapping(value = "/stream/{executionId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ExecutionEvent> getEventStream(@PathVariable String executionId) {
        return executionStreamingService.getEventStream(executionId);
    }

    @DeleteMapping("/execute/{executionId}")
    public ResponseEntity<String> cancelExecution(@PathVariable String executionId) {
        boolean cancelled = executionStreamingService.cancelExecution(executionId);
        if (cancelled) {
            return ResponseEntity.ok("Execution cancelled successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/status")
    public ResponseEntity<ExecutionStatus> getStatus() {
        ExecutionStatus status = new ExecutionStatus(
                executionStreamingService.getRunningExecutionCount(),
                executionStreamingService.getConnectedClientCount()
        );
        return ResponseEntity.ok(status);
    }

    public record ExecutionStatus(int runningExecutions, int connectedClients) {}
}
