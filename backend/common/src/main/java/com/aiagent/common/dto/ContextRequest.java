package com.aiagent.common.dto;

public class ContextRequest {
    private String task;

    public ContextRequest() {}

    public ContextRequest(String task) {
        this.task = task;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }
}
