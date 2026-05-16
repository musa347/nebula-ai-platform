package com.aiagent.common.model;

import com.aiagent.common.enums.ExecutionState;

public class ExecutionSession {
    private String executionId;
    private ExecutionState currentState;
    private long createdAt;
    private long updatedAt;

    public ExecutionSession() {}

    public ExecutionSession(String executionId, ExecutionState currentState, long createdAt, long updatedAt) {
        this.executionId = executionId;
        this.currentState = currentState;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public ExecutionState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(ExecutionState currentState) {
        this.currentState = currentState;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}