package com.aiagent.common.model;

import java.util.ArrayList;
import java.util.List;

public class ExecutionScore {
    private String executionId;
    private double score;
    private List<String> reasons = new ArrayList<>();

    public ExecutionScore() {
    }

    public ExecutionScore(String executionId, double score) {
        this.executionId = executionId;
        this.score = score;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }
}
