package com.aiagent.orchestrator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for AI patch generation.
 * Defines the strict contract between LLM and system for patch requests.
 */
public class AiPatchRequest {

    @JsonProperty("task")
    private String task;

    @JsonProperty("file")
    private String file;

    @JsonProperty("context")
    private String context;

    @JsonProperty("filePreview")
    private String filePreview;

    public AiPatchRequest() {
    }

    public AiPatchRequest(String task, String file, String context, String filePreview) {
        this.task = task;
        this.file = file;
        this.context = context;
        this.filePreview = filePreview;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getFilePreview() {
        return filePreview;
    }

    public void setFilePreview(String filePreview) {
        this.filePreview = filePreview;
    }

    @Override
    public String toString() {
        return "AiPatchRequest{" +
                "task='" + task + '\'' +
                ", file='" + file + '\'' +
                ", context='" + context + '\'' +
                ", filePreview='" + (filePreview != null ? filePreview.length() + " chars" : "null") + '\'' +
                '}';
    }
}