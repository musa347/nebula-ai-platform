package com.aiagent.orchestrator.model;

import java.util.List;

public class RecoveryStrategy {
    private String failureType;
    private String rootCause;
    private int maxRetries;
    private List<RecoveryAction> actions;
    
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
    
    public int getMaxRetries() {
        return maxRetries;
    }
    
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }
    
    public List<RecoveryAction> getActions() {
        return actions;
    }
    
    public void setActions(List<RecoveryAction> actions) {
        this.actions = actions;
    }
}
