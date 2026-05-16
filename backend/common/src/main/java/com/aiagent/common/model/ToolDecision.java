package com.aiagent.common.model;

import com.aiagent.common.enums.ToolType;

public class ToolDecision {
    private ToolType toolType;
    private String reason;

    public ToolDecision() {}

    public ToolDecision(ToolType toolType, String reason) {
        this.toolType = toolType;
        this.reason = reason;
    }

    public ToolType getToolType() {
        return toolType;
    }

    public void setToolType(ToolType toolType) {
        this.toolType = toolType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}