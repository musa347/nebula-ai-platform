package com.aiagent.orchestrator.autonomous;

public class NextAction {
    private ActionType type;
    private String reason;
    
    public NextAction(ActionType type, String reason) {
        this.type = type;
        this.reason = reason;
    }
    
    public ActionType getType() {
        return type;
    }
    
    public String getReason() {
        return reason;
    }
}
