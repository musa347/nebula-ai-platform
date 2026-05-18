package com.aiagent.orchestrator.learning;

public class LearningSignal {
    private String executionId;
    private boolean success;
    private String toolUsed;
    private String state;
    private double score;
    private String outcome;
    
    public LearningSignal() {}
    
    public LearningSignal(String executionId, boolean success, String toolUsed, 
                          String state, double score, String outcome) {
        this.executionId = executionId;
        this.success = success;
        this.toolUsed = toolUsed;
        this.state = state;
        this.score = score;
        this.outcome = outcome;
    }
    
    public String getExecutionId() {
        return executionId;
    }
    
    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getToolUsed() {
        return toolUsed;
    }
    
    public void setToolUsed(String toolUsed) {
        this.toolUsed = toolUsed;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public double getScore() {
        return score;
    }
    
    public void setScore(double score) {
        this.score = score;
    }
    
    public String getOutcome() {
        return outcome;
    }
    
    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }
}
