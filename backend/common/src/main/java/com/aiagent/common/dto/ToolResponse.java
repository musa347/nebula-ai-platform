package com.aiagent.common.dto;

import com.aiagent.common.enums.ExecutionStatus;
import com.aiagent.common.enums.ToolType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents the response from a tool execution.
 */
public class ToolResponse {
    
    private String id;
    
    private String toolName;
    
    private ToolType toolType;
    
    @NotNull
    private ExecutionStatus status;
    
    private Object result;
    
    private String errorMessage;
    
    private String errorCode;
    
    private LocalDateTime timestamp = LocalDateTime.now();
    
    private Long executionTimeMs;
    
    private String sessionId;
    
    private String requestId;
    
    private Map<String, Object> metadata;
    

    public ToolResponse() {}

    public ToolResponse(String id, String toolName, ToolType toolType, ExecutionStatus status, 
                     Object result, String errorMessage, String errorCode, LocalDateTime timestamp, 
                     Long executionTimeMs, String sessionId, String requestId, 
                     Map<String, Object> metadata) {
        this.id = id;
        this.toolName = toolName;
        this.toolType = toolType;
        this.status = status != null ? status : ExecutionStatus.FAILED;
        this.result = result;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.executionTimeMs = executionTimeMs;
        this.sessionId = sessionId;
        this.requestId = requestId;
        this.metadata = metadata;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }
    
    public ToolType getToolType() { return toolType; }
    public void setToolType(ToolType toolType) { this.toolType = toolType; }
    
    public ExecutionStatus getStatus() { return status; }
    public void setStatus(ExecutionStatus status) { this.status = status; }
    
    public Object getResult() { return result; }
    public void setResult(Object result) { this.result = result; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public Long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(Long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    

    public boolean isSuccess() {
        return ExecutionStatus.SUCCESS.equals(status);
    }

    public boolean isFailure() {
        return ExecutionStatus.FAILED.equals(status) || ExecutionStatus.TIMEOUT.equals(status);
    }

    @SuppressWarnings("unchecked")
    public <T> T getResultAs(Class<T> type) {
        if (result == null) {
            return null;
        }
        
        if (type.isInstance(result)) {
            return (T) result;
        }
        
        if (type == String.class) {
            return (T) result.toString();
        }
        
        return null;
    }
}
