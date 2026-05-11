package com.aiagent.mcp.service;

import com.aiagent.common.dto.ToolRequest;
import com.aiagent.common.dto.ToolResponse;
import com.aiagent.common.enums.ExecutionStatus;
import com.aiagent.common.enums.ToolType;
import com.aiagent.mcp.dto.PatchResponse;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for handling filesystem tool operations(read, patch).
 */
@Service
public class FileSystemToolService {
    
    private final FileSystemService fileSystemService;
    private final FilesystemPatchService filesystemPatchService;
    
    public FileSystemToolService(FileSystemService fileSystemService, FilesystemPatchService filesystemPatchService) {
        this.fileSystemService = fileSystemService;
        this.filesystemPatchService = filesystemPatchService;
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

    public ToolResponse executeFileSystemPatch(ToolRequest request) {
        Map<String, Object> parameters = request.getParameters();
        String path = (String) parameters.get("path");
        String patch = (String) parameters.get("patch");
        
        if (path == null || path.trim().isEmpty()) {
            return createErrorResponse(request, "Missing required parameter: path");
        }
        
        if (patch == null || patch.trim().isEmpty()) {
            return createErrorResponse(request, "Missing required parameter: patch");
        }
        
        try {
            PatchResponse patchResponse = filesystemPatchService.applyPatch(path, patch);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", patchResponse.isSuccess());
            result.put("diff", patchResponse.getDiff());
            result.put("backupCreated", patchResponse.isBackupCreated());
            result.put("backupPath", patchResponse.getBackupPath());
            result.put("message", patchResponse.getMessage());
            result.put("path", path);
            
            return createSuccessResponse(request, result);
            
        } catch (Exception e) {
            return createErrorResponse(request, "Failed to apply patch: " + e.getMessage());
        }
    }

    public ToolResponse execute(ToolRequest request) {
        return switch (request.getToolType()) {
            case FILESYSTEM_READ -> executeFileSystemRead(request);
            case FILESYSTEM_PATCH -> executeFileSystemPatch(request);
            default -> createErrorResponse(request, "Unsupported tool type: " + request.getToolType());
        };
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
