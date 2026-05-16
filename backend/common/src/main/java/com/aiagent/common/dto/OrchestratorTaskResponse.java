package com.aiagent.common.dto;

import com.aiagent.common.enums.ExecutionState;
import com.aiagent.common.model.LoadedContext;
import java.util.List;

public class OrchestratorTaskResponse {
    private String executionId;
    private List<ExecutionState> completedStates;
    private List<LoadedContext> contexts;

    public OrchestratorTaskResponse() {}

    public OrchestratorTaskResponse(String executionId, List<ExecutionState> completedStates) {
        this.executionId = executionId;
        this.completedStates = completedStates;
    }

    public OrchestratorTaskResponse(String executionId, List<ExecutionState> completedStates, List<LoadedContext> contexts) {
        this.executionId = executionId;
        this.completedStates = completedStates;
        this.contexts = contexts;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public List<ExecutionState> getCompletedStates() {
        return completedStates;
    }

    public void setCompletedStates(List<ExecutionState> completedStates) {
        this.completedStates = completedStates;
    }

    public List<LoadedContext> getContexts() {
        return contexts;
    }

    public void setContexts(List<LoadedContext> contexts) {
        this.contexts = contexts;
    }
}