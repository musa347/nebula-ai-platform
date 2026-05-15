package com.aiagent.common.model;

import java.util.ArrayList;
import java.util.List;

public class ExecutionInsight {
    private String type;
    private String message;
    private int occurrenceCount;
    private List<String> executionIds = new ArrayList<>();

    public ExecutionInsight() {
    }

    public ExecutionInsight(String type, String message, int occurrenceCount) {
        this.type = type;
        this.message = message;
        this.occurrenceCount = occurrenceCount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getOccurrenceCount() {
        return occurrenceCount;
    }

    public void setOccurrenceCount(int occurrenceCount) {
        this.occurrenceCount = occurrenceCount;
    }

    public List<String> getExecutionIds() {
        return executionIds;
    }

    public void setExecutionIds(List<String> executionIds) {
        this.executionIds = executionIds;
    }
}
