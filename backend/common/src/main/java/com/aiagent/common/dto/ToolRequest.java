package com.aiagent.common.dto;

import com.aiagent.common.enums.ToolType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents a request to execute a tool.
 */
public class ToolRequest {
    
    @NotBlank(message = "Tool name cannot be blank")
    private String toolName;
    
    @NotNull(message = "Tool type cannot be null")
    private ToolType toolType;
    
    private String id;
    
    private LocalDateTime timestamp = LocalDateTime.now();
    
    private Map<String, Object> parameters;
    
    private String sessionId;
    
    private String requestId;
    
    private Map<String, String> metadata;
    

    public ToolRequest() {}
    

    public ToolRequest(String toolName, ToolType toolType, String id, LocalDateTime timestamp, 
                    Map<String, Object> parameters, String sessionId, String requestId, 
                    Map<String, String> metadata) {
        this.toolName = toolName;
        this.toolType = toolType;
        this.id = id;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.parameters = parameters;
        this.sessionId = sessionId;
        this.requestId = requestId;
        this.metadata = metadata;
    }

    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }
    
    public ToolType getToolType() { return toolType; }
    public void setToolType(ToolType toolType) { this.toolType = toolType; }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    
    public Map<String, String> getMetadata() { return metadata; }
    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
    

    public boolean isValid() {
        return toolName != null && !toolName.trim().isEmpty() && toolType != null;
    }
    

    @SuppressWarnings("unchecked")
    public <T> T getParameter(String key, Class<T> type) {
        if (parameters == null || !parameters.containsKey(key)) {
            return null;
        }
        
        Object value = parameters.get(key);
        if (value == null) {
            return null;
        }
        
        if (type.isInstance(value)) {
            return (T) value;
        }
        
        // Handle basic type conversions
        if (type == String.class) {
            return (T) value.toString();
        }
        
        return null;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String toolName;
        private ToolType toolType;
        private String id;
        private LocalDateTime timestamp = LocalDateTime.now();
        private Map<String, Object> parameters;
        private String sessionId;
        private String requestId;
        private Map<String, String> metadata;
        
        public Builder toolName(String toolName) {
            this.toolName = toolName;
            return this;
        }
        
        public Builder toolType(ToolType toolType) {
            this.toolType = toolType;
            return this;
        }
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder parameters(Map<String, Object> parameters) {
            this.parameters = parameters;
            return this;
        }
        
        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }
        
        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }
        
        public Builder metadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public ToolRequest build() {
            return new ToolRequest(toolName, toolType, id, timestamp, parameters, sessionId, requestId, metadata);
        }
    }
}
