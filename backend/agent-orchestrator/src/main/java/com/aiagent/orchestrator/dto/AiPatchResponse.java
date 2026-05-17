package com.aiagent.orchestrator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for AI patch generation.
 * Defines the strict contract between LLM and system for patch responses.
 */
public class AiPatchResponse {

    @JsonProperty("file")
    private String file;

    @JsonProperty("description")
    private String description;

    @JsonProperty("suggestedChange")
    private String suggestedChange;

    public AiPatchResponse() {
    }

    public AiPatchResponse(String file, String description, String suggestedChange) {
        this.file = file;
        this.description = description;
        this.suggestedChange = suggestedChange;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSuggestedChange() {
        return suggestedChange;
    }

    public void setSuggestedChange(String suggestedChange) {
        this.suggestedChange = suggestedChange;
    }

    @Override
    public String toString() {
        return "AiPatchResponse{" +
                "file='" + file + '\'' +
                ", description='" + description + '\'' +
                ", suggestedChange='" + (suggestedChange != null ? suggestedChange.length() + " chars" : "null") + '\'' +
                '}';
    }
}