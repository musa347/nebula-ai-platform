package com.aiagent.orchestrator.dto;

public class AiPlanResponse {

    private String refinedPlanJson;

    public AiPlanResponse() {
    }

    public AiPlanResponse(String refinedPlanJson) {
        this.refinedPlanJson = refinedPlanJson;
    }

    public String getRefinedPlanJson() {
        return refinedPlanJson;
    }

    public void setRefinedPlanJson(String refinedPlanJson) {
        this.refinedPlanJson = refinedPlanJson;
    }
}