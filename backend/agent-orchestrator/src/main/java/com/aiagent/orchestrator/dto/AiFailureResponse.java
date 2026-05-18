package com.aiagent.orchestrator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for AI failure analysis.
 * Defines the strict contract for failure analysis responses.
 */
public class AiFailureResponse {

    @JsonProperty("failureType")
    private String failureType;

    @JsonProperty("rootCause")
    private String rootCause;

    @JsonProperty("suggestedRecovery")
    private String suggestedRecovery;

    public AiFailureResponse() {
    }

    public AiFailureResponse(String failureType, String rootCause, String suggestedRecovery) {
        this.failureType = failureType;
        this.rootCause = rootCause;
        this.suggestedRecovery = suggestedRecovery;
    }

    public String getFailureType() {
        return failureType;
    }

    public void setFailureType(String failureType) {
        this.failureType = failureType;
    }

    public String getRootCause() {
        return rootCause;
    }

    public void setRootCause(String rootCause) {
        this.rootCause = rootCause;
    }

    public String getSuggestedRecovery() {
        return suggestedRecovery;
    }

    public void setSuggestedRecovery(String suggestedRecovery) {
        this.suggestedRecovery = suggestedRecovery;
    }

    @Override
    public String toString() {
        return "AiFailureResponse{" +
                "failureType='" + failureType + '\'' +
                ", rootCause='" + rootCause + '\'' +
                ", suggestedRecovery='" + suggestedRecovery + '\'' +
                '}';
    }
}