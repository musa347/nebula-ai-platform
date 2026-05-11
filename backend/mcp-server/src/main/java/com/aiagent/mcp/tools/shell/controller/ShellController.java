package com.aiagent.mcp.tools.shell.controller;

import com.aiagent.common.dto.ShellExecuteRequest;
import com.aiagent.common.dto.ShellExecuteResponse;
import com.aiagent.mcp.tools.shell.service.ShellExecutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for shell command execution.
 * Provides secure, sandboxed command execution endpoint.
 */
@RestController
@RequestMapping("/api/tools/shell")
public class ShellController {

    private final ShellExecutionService shellExecutionService;

    public ShellController(ShellExecutionService shellExecutionService) {
        this.shellExecutionService = shellExecutionService;
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
}
