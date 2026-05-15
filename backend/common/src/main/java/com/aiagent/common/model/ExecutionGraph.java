package com.aiagent.common.model;

import java.util.ArrayList;
import java.util.List;

public class ExecutionGraph {
    private String executionId;
    private List<ExecutionNode> nodes = new ArrayList<>();

    public ExecutionGraph() {
    }

    public ExecutionGraph(String executionId) {
        this.executionId = executionId;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public List<ExecutionNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<ExecutionNode> nodes) {
        this.nodes = nodes;
    }
}
