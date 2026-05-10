package com.aiagent.mcp.service;

import com.aiagent.common.dto.ToolRequest;
import com.aiagent.common.dto.ToolResponse;
import com.aiagent.common.enums.ExecutionStatus;
import com.aiagent.common.enums.ToolType;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for handling filesystem tool operations.
 */
@Service
public class FileSystemToolService {
    
    private final FileSystemService fileSystemService;
    
    public FileSystemToolService(FileSystemService fileSystemService) {
        this.fileSystemService = fileSystemService;
    }

    public ToolResponse executeFileSystemRead(ToolRequest request) {
        Map<String, Object> parameters = request.getParameters();
        String path = (String) parameters.get("path");
        
        if (path == null || path.trim().isEmpty()) {
            return createErrorResponse(request, "Missing required parameter: path");
        }
        
        try {
            String content = fileSystemService.readFile(path);
            
            Map<String, Object> result = new HashMap<>();
            result.put("content", content);
            result.put("path", path);
            
            return createSuccessResponse(request, result);
            
        } catch (Exception e) {
            return createErrorResponse(request, "Failed to read file: " + e.getMessage());
        }
    }

    private ToolResponse createSuccessResponse(ToolRequest request, Object result) {
        return new ToolResponse(
            request.getId(),
            request.getToolName(),
            request.getToolType(),
            ExecutionStatus.SUCCESS,
            result,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
    }

    private ToolResponse createErrorResponse(ToolRequest request, String errorMessage) {
        return new ToolResponse(
            request.getId(),
            request.getToolName(),
            request.getToolType(),
            ExecutionStatus.FAILED,
            null,
            errorMessage,
            null,
            null,
            null,
            null,
            null,
            null
        );
    }
}
