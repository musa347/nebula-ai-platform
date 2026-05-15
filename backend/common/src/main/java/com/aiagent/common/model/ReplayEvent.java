package com.aiagent.common.model;

public class ReplayEvent {
    private String executionId;
    private String type;
    private String message;
    private long timestamp;

    public ReplayEvent() {
    }

    public ReplayEvent(String executionId, String type, String message, long timestamp) {
        this.executionId = executionId;
        this.type = type;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
