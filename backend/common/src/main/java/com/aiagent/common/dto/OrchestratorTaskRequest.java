package com.aiagent.common.dto;

public class OrchestratorTaskRequest {
    private String task;
    private String description;
    private String targetFile;

    public OrchestratorTaskRequest() {}

    public OrchestratorTaskRequest(String task) {
        this.task = task;
    }

    public String getTask() {
        return task != null ? task : description;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTargetFile() {
        return targetFile;
    }

    public void setTargetFile(String targetFile) {
        this.targetFile = targetFile;
    }
}