package com.aiagent.orchestrator.embedding;

public class SemanticMatch {
    private String id;
    private double score;
    private String sourceType;
    
    public SemanticMatch() {}
    
    public SemanticMatch(String id, double score, String sourceType) {
        this.id = id;
        this.score = score;
        this.sourceType = sourceType;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public double getScore() {
        return score;
    }
    
    public void setScore(double score) {
        this.score = score;
    }
    
    public String getSourceType() {
        return sourceType;
    }
    
    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }
}
