package com.aiagent.common.model;

import java.util.List;

public class ExecutionPlan {
    private String executionId;
    private List<PlanStep> steps;

    public ExecutionPlan() {}

    public ExecutionPlan(String executionId, List<PlanStep> steps) {
        this.executionId = executionId;
        this.steps = steps;
    }

    public String getExecutionId() { return executionId; }
    public void setExecutionId(String executionId) { this.executionId = executionId; }
    public List<PlanStep> getSteps() { return steps; }
    public void setSteps(List<PlanStep> steps) { this.steps = steps; }
}