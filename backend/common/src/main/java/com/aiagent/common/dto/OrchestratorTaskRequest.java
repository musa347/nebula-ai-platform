package com.aiagent.common.dto;

public class OrchestratorTaskRequest {
    private String task;

    public OrchestratorTaskRequest() {}

    public OrchestratorTaskRequest(String task) {
        this.task = task;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }
}