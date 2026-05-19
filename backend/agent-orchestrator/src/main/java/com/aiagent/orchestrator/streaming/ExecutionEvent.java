package com.aiagent.orchestrator.streaming;

import java.util.HashMap;
import java.util.Map;

public class ExecutionEvent {
    private String executionId;
    private ExecutionEventType type;
    private String message;
    private long timestamp;
    private Map<String, Object> metadata;
    
    public ExecutionEvent(String executionId, ExecutionEventType type, String message) {
        this.executionId = executionId;
        this.type = type;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
        this.metadata = new HashMap<>();
    }
    
    public ExecutionEvent(String executionId, ExecutionEventType type, String message, Map<String, Object> metadata) {
        this.executionId = executionId;
        this.type = type;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
    }
    
    public String getExecutionId() {
        return executionId;
    }
    
    public ExecutionEventType getType() {
        return type;
    }
    
    public String getMessage() {
        return message;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }
}
