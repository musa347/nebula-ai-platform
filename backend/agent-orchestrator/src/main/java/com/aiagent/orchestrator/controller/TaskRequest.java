package com.aiagent.orchestrator.controller;

public class TaskRequest {
    private String task;
    private String workingDirectory;
    private Integer maxRetries;

    public String getTask() { return task; }
    public void setTask(String task) { this.task = task; }

    public String getWorkingDirectory() { return workingDirectory; }
    public void setWorkingDirectory(String workingDirectory) { this.workingDirectory = workingDirectory; }

    public Integer getMaxRetries() { return maxRetries; }
    public void setMaxRetries(Integer maxRetries) { this.maxRetries = maxRetries; }
}
