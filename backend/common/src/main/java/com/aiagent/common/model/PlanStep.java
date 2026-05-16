package com.aiagent.common.model;

import com.aiagent.common.enums.ToolType;

public class PlanStep {
    private int order;
    private String description;
    private ToolType toolType;
    private String target;

    public PlanStep() {}

    public PlanStep(int order, String description, ToolType toolType, String target) {
        this.order = order;
        this.description = description;
        this.toolType = toolType;
        this.target = target;
    }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ToolType getToolType() { return toolType; }
    public void setToolType(ToolType toolType) { this.toolType = toolType; }
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
}