package com.aiagent.mcp.controller;

import com.aiagent.common.dto.ToolRequest;
import com.aiagent.common.dto.ToolResponse;
import com.aiagent.common.enums.ToolType;
import com.aiagent.mcp.service.FileSystemToolService;
import com.aiagent.mcp.service.FileSystemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for file system operations.
 */
@RestController
@RequestMapping("/api/tools")
public class FileSystemController {
    
    private final FileSystemToolService fileSystemToolService;
    private final FileSystemService fileSystemService;
    
    public FileSystemController(FileSystemToolService fileSystemToolService, FileSystemService fileSystemService) {
        this.fileSystemToolService = fileSystemToolService;
        this.fileSystemService = fileSystemService;
    }

    @PostMapping("/execute")
    public ResponseEntity<ToolResponse> execute(@RequestBody ToolRequest request) {
        try {
            if (request.getToolType() == ToolType.FILESYSTEM_READ) {
                ToolResponse response = fileSystemToolService.executeFileSystemRead(request);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(
                    new ToolResponse(
                        request.getId(),
                        request.getToolName(),
                        request.getToolType(),
                        com.aiagent.common.enums.ExecutionStatus.FAILED,
                        null,
                        "Unsupported tool type: " + request.getToolType(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                    )
                );
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    new ToolResponse(
                        request.getId(),
                        request.getToolName(),
                        request.getToolType(),
                        com.aiagent.common.enums.ExecutionStatus.FAILED,
                        null,
                        "Internal server error: " + e.getMessage(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                    )
            );
        }
    }
    @GetMapping("/filesystem/metadata")
    public ResponseEntity<?> getFileMetadata(@RequestParam String path) {
        try {
            return ResponseEntity.ok(fileSystemService.getFileMetadata(path));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "Failed to get file metadata: " + e.getMessage())
            );
        }
    }
}
