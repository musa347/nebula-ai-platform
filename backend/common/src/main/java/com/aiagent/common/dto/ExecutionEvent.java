package com.aiagent.common.dto;

/**
 * Represents a real-time execution event for streaming shell command output.
 * This enables progressive execution feedback to make the agent feel alive and responsive.
 */
public class ExecutionEvent {
    
    private String executionId;
    private ExecutionEventType type;
    private String message;
    private Long timestamp;
    

    private ExecutionEvent() {}

    public String getExecutionId() {
        return executionId;
    }
    
    public ExecutionEventType getType() {
        return type;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Long getTimestamp() {
        return timestamp;
    }
    
    // Setters
    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }
    
    public void setType(ExecutionEventType type) {
        this.type = type;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final ExecutionEvent event = new ExecutionEvent();
        
        public Builder executionId(String executionId) {
            event.executionId = executionId;
            return this;
        }
        
        public Builder type(ExecutionEventType type) {
            event.type = type;
            return this;
        }
        
        public Builder message(String message) {
            event.message = message;
            return this;
        }
        
        public Builder timestamp(Long timestamp) {
            event.timestamp = timestamp;
            return this;
        }
        
        public ExecutionEvent build() {
            if (event.timestamp == null) {
                event.timestamp = System.currentTimeMillis();
            }
            return event;
        }
    }
    
    @Override
    public String toString() {
        return "ExecutionEvent{" +
                "executionId='" + executionId + '\'' +
                ", type=" + type +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
