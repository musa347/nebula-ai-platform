package com.aiagent.common.model;

import com.aiagent.common.enums.ToolType;

public class ToolExecutionStats {
    private ToolType toolType;
    private int successCount;
    private int failureCount;
    private long avgExecutionTimeMs;

    public ToolExecutionStats(ToolType toolType) {
        this.toolType = toolType;
        this.successCount = 0;
        this.failureCount = 0;
        this.avgExecutionTimeMs = 0;
    }

    public double getSuccessRate() {
        int total = successCount + failureCount;
        return total == 0 ? 0.0 : (double) successCount / total;
    }

    public double getFailureRate() {
        int total = successCount + failureCount;
        return total == 0 ? 0.0 : (double) failureCount / total;
    }

    public ToolType getToolType() { return toolType; }
    public void setToolType(ToolType toolType) { this.toolType = toolType; }
    public int getSuccessCount() { return successCount; }
    public void setSuccessCount(int successCount) { this.successCount = successCount; }
    public int getFailureCount() { return failureCount; }
    public void setFailureCount(int failureCount) { this.failureCount = failureCount; }
    public long getAvgExecutionTimeMs() { return avgExecutionTimeMs; }
    public void setAvgExecutionTimeMs(long avgExecutionTimeMs) { this.avgExecutionTimeMs = avgExecutionTimeMs; }
}