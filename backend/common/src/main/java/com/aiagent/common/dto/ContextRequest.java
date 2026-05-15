package com.aiagent.common.dto;

public class ContextRequest {
    private String task;
    private String executionId;

    public ContextRequest() {}

    public ContextRequest(String task) {
        this.task = task;
    }
    
    public ContextRequest(String task, String executionId) {
        this.task = task;
        this.executionId = executionId;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }
    
    public String getExecutionId() {
        return executionId;
    }
    
    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }
}
