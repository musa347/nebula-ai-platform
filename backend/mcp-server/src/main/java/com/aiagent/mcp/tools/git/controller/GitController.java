package com.aiagent.mcp.tools.git.controller;

import com.aiagent.common.dto.GitDiffResponse;
import com.aiagent.common.dto.GitRestoreResponse;
import com.aiagent.common.dto.GitStatusResponse;
import com.aiagent.common.dto.ToolRequest;
import com.aiagent.common.dto.ToolResponse;
import com.aiagent.common.enums.ExecutionStatus;
import com.aiagent.common.enums.ToolType;
import com.aiagent.common.model.GitCheckpoint;
import com.aiagent.mcp.tools.git.checkpoint.GitCheckpointManager;
import com.aiagent.mcp.tools.git.service.GitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/git")
public class GitController {

    private static final Logger log = LoggerFactory.getLogger(GitController.class);
    private final GitService gitService;
    private final GitCheckpointManager checkpointManager;

    public GitController(GitService gitService, GitCheckpointManager checkpointManager) {
        this.gitService = gitService;
        this.checkpointManager = checkpointManager;
    }

    @PostMapping("/status")
    public ResponseEntity<ToolResponse> status(@RequestBody ToolRequest request) {
        try {
            String workspacePath = request.getParameter("workspacePath", String.class);
            if (workspacePath == null) {
                return ResponseEntity.badRequest().body(createErrorResponse(request, "workspacePath required"));
            }
            
            GitStatusResponse status = gitService.getStatus(workspacePath);
            
            ToolResponse response = new ToolResponse();
            response.setId(request.getId());
            response.setToolName(ToolType.GIT_STATUS.getToolName());
            response.setToolType(ToolType.GIT_STATUS);
            response.setStatus(ExecutionStatus.SUCCESS);
            response.setResult(status);
            response.setSessionId(request.getSessionId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Git status failed", e);
            return ResponseEntity.ok(createErrorResponse(request, e.getMessage()));
        }
    }

    @PostMapping("/diff")
    public ResponseEntity<ToolResponse> diff(@RequestBody ToolRequest request) {
        try {
            String workspacePath = request.getParameter("workspacePath", String.class);
            if (workspacePath == null) {
                return ResponseEntity.badRequest().body(createErrorResponse(request, "workspacePath required"));
            }
            
            @SuppressWarnings("unchecked")
            List<String> files = (List<String>) request.getParameters().get("files");
            
            GitDiffResponse diff = gitService.getDiff(workspacePath, files);
            
            ToolResponse response = new ToolResponse();
            response.setId(request.getId());
            response.setToolName(ToolType.GIT_DIFF.getToolName());
            response.setToolType(ToolType.GIT_DIFF);
            response.setStatus(ExecutionStatus.SUCCESS);
            response.setResult(diff);
            response.setSessionId(request.getSessionId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Git diff failed", e);
            return ResponseEntity.ok(createErrorResponse(request, e.getMessage()));
        }
    }

    @PostMapping("/restore")
    public ResponseEntity<ToolResponse> restore(@RequestBody ToolRequest request) {
        try {
            String workspacePath = request.getParameter("workspacePath", String.class);
            if (workspacePath == null) {
                return ResponseEntity.badRequest().body(createErrorResponse(request, "workspacePath required"));
            }
            
            @SuppressWarnings("unchecked")
            List<String> files = (List<String>) request.getParameters().get("files");
            
            GitRestoreResponse restore = gitService.restore(workspacePath, files);
            
            ToolResponse response = new ToolResponse();
            response.setId(request.getId());
            response.setToolName(ToolType.GIT_RESTORE.getToolName());
            response.setToolType(ToolType.GIT_RESTORE);
            response.setStatus(restore.isSuccess() ? ExecutionStatus.SUCCESS : ExecutionStatus.FAILED);
            response.setResult(restore);
            response.setSessionId(request.getSessionId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Git restore failed", e);
            return ResponseEntity.ok(createErrorResponse(request, e.getMessage()));
        }
    }

    @PostMapping("/checkpoint/create")
    public ResponseEntity<GitCheckpoint> createCheckpoint(@RequestBody ToolRequest request) {
        try {
            String workspacePath = request.getParameter("workspacePath", String.class);
            String description = request.getParameter("description", String.class);
            
            GitCheckpoint checkpoint = checkpointManager.createCheckpoint(
                    workspacePath, 
                    request.getSessionId(), 
                    description
            );
            
            return ResponseEntity.ok(checkpoint);
        } catch (Exception e) {
            log.error("Checkpoint creation failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/checkpoint/restore/{checkpointId}")
    public ResponseEntity<String> restoreCheckpoint(
            @PathVariable String checkpointId,
            @RequestBody ToolRequest request) {
        try {
            String workspacePath = request.getParameter("workspacePath", String.class);
            checkpointManager.restoreCheckpoint(workspacePath, checkpointId);
            return ResponseEntity.ok("Checkpoint restored successfully");
        } catch (Exception e) {
            log.error("Checkpoint restore failed", e);
            return ResponseEntity.internalServerError().body("Failed to restore checkpoint: " + e.getMessage());
        }
    }

    @DeleteMapping("/checkpoint/{checkpointId}")
    public ResponseEntity<String> dropCheckpoint(@PathVariable String checkpointId, @RequestParam String workspacePath) {
        try {
            checkpointManager.dropCheckpoint(workspacePath, checkpointId);
            return ResponseEntity.ok("Checkpoint dropped successfully");
        } catch (Exception e) {
            log.error("Checkpoint drop failed", e);
            return ResponseEntity.internalServerError().body("Failed to drop checkpoint: " + e.getMessage());
        }
    }

    @GetMapping("/checkpoint/session/{sessionId}")
    public ResponseEntity<List<GitCheckpoint>> getSessionCheckpoints(@PathVariable String sessionId) {
        return ResponseEntity.ok(checkpointManager.getCheckpointsBySession(sessionId));
    }

    private ToolResponse createErrorResponse(ToolRequest request, String errorMessage) {
        ToolResponse response = new ToolResponse();
        response.setId(request.getId());
        response.setToolName(request.getToolName());
        response.setToolType(request.getToolType());
        response.setStatus(ExecutionStatus.FAILED);
        response.setErrorMessage(errorMessage);
        response.setSessionId(request.getSessionId());
        return response;
    }
}
