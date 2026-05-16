package com.aiagent.common.dto;

import com.aiagent.common.enums.ExecutionState;
import java.util.List;

public class OrchestratorTaskResponse {
    private String executionId;
    private List<ExecutionState> completedStates;

    public OrchestratorTaskResponse() {}

    public OrchestratorTaskResponse(String executionId, List<ExecutionState> completedStates) {
        this.executionId = executionId;
        this.completedStates = completedStates;
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
}