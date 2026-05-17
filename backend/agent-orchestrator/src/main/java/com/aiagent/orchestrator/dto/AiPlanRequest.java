package com.aiagent.orchestrator.dto;

public class AiPlanRequest {

    private String task;
    private String deterministicPlanJson;

    public AiPlanRequest() {
    }

    public AiPlanRequest(String task, String deterministicPlanJson) {
        this.task = task;
        this.deterministicPlanJson = deterministicPlanJson;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getDeterministicPlanJson() {
        return deterministicPlanJson;
    }

    public void setDeterministicPlanJson(String deterministicPlanJson) {
        this.deterministicPlanJson = deterministicPlanJson;
    }
}