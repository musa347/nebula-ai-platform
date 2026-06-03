package com.aiagent.common.dto;

import java.util.List;
import java.util.Map;

public class OrchestratorTaskRequest {
    private String task;
    private String description;
    private String targetFile;
    private String workspacePath;
    private List<String> sourceFiles;
    private Map<String, Object> workspaceMetadata;

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

    public String getWorkspacePath() {
        return workspacePath;
    }

    public void setWorkspacePath(String workspacePath) {
        this.workspacePath = workspacePath;
    }

    public List<String> getSourceFiles() {
        return sourceFiles;
    }

    public void setSourceFiles(List<String> sourceFiles) {
        this.sourceFiles = sourceFiles;
    }

    public Map<String, Object> getWorkspaceMetadata() {
        return workspaceMetadata;
    }

    public void setWorkspaceMetadata(Map<String, Object> workspaceMetadata) {
        this.workspaceMetadata = workspaceMetadata;
    }
}