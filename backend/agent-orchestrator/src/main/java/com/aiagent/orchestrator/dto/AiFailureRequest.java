package com.aiagent.orchestrator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for AI failure analysis.
 * Defines the strict contract for failure analysis requests.
 */
public class AiFailureRequest {

    @JsonProperty("task")
    private String task;

    @JsonProperty("executionState")
    private String executionState;

    @JsonProperty("toolName")
    private String toolName;

    @JsonProperty("stderr")
    private String stderr;

    @JsonProperty("stdout")
    private String stdout;

    @JsonProperty("context")
    private String context;

    public AiFailureRequest() {
    }

    public AiFailureRequest(String task, String executionState, String toolName, 
                           String stderr, String stdout, String context) {
        this.task = task;
        this.executionState = executionState;
        this.toolName = toolName;
        this.stderr = stderr;
        this.stdout = stdout;
        this.context = context;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getExecutionState() {
        return executionState;
    }

    public void setExecutionState(String executionState) {
        this.executionState = executionState;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getStderr() {
        return stderr;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    public String getStdout() {
        return stdout;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    @Override
    public String toString() {
        return "AiFailureRequest{" +
                "task='" + task + '\'' +
                ", executionState='" + executionState + '\'' +
                ", toolName='" + toolName + '\'' +
                ", stderr='" + (stderr != null ? stderr.length() + " chars" : "null") + '\'' +
                ", stdout='" + (stdout != null ? stdout.length() + " chars" : "null") + '\'' +
                ", context='" + (context != null ? context.length() + " chars" : "null") + '\'' +
                '}';
    }
}