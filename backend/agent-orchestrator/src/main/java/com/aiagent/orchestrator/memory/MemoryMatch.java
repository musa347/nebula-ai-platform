package com.aiagent.orchestrator.memory;

public class MemoryMatch {
    private String memoryId;
    private double score;
    private String outcome;
    
    public MemoryMatch() {}
    
    public MemoryMatch(String memoryId, double score, String outcome) {
        this.memoryId = memoryId;
        this.score = score;
        this.outcome = outcome;
    }
    
    public String getMemoryId() {
        return memoryId;
    }
    
    public void setMemoryId(String memoryId) {
        this.memoryId = memoryId;
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
