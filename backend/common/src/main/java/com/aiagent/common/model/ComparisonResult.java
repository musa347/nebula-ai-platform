package com.aiagent.common.model;

import java.util.ArrayList;
import java.util.List;

public class ComparisonResult {
    private String executionA;
    private String executionB;
    private List<Difference> differences = new ArrayList<>();

    public ComparisonResult() {
    }

    public ComparisonResult(String executionA, String executionB) {
        this.executionA = executionA;
        this.executionB = executionB;
    }

    public String getExecutionA() {
        return executionA;
    }

    public void setExecutionA(String executionA) {
        this.executionA = executionA;
    }

    public String getExecutionB() {
        return executionB;
    }

    public void setExecutionB(String executionB) {
        this.executionB = executionB;
    }

    public List<Difference> getDifferences() {
        return differences;
    }

    public void setDifferences(List<Difference> differences) {
        this.differences = differences;
    }
}
