package com.aiagent.orchestrator.memory;

import java.util.List;

public class SemanticMemoryEntry {
    private String id;
    private String executionId;
    private String task;
    private String outcome;
    private String summary;
    private String sourceType;
    private List<Float> embedding;
    
    public SemanticMemoryEntry() {}
    
    public SemanticMemoryEntry(String id, String executionId, String task, String outcome, 
                               String summary, String sourceType, List<Float> embedding) {
        this.id = id;
        this.executionId = executionId;
        this.task = task;
        this.outcome = outcome;
        this.summary = summary;
        this.sourceType = sourceType;
        this.embedding = embedding;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getExecutionId() {
        return executionId;
    }
    
    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }
    
    public String getTask() {
        return task;
    }
    
    public void setTask(String task) {
        this.task = task;
    }
    
    public String getOutcome() {
        return outcome;
    }
    
    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }
    
    public String getSummary() {
        return summary;
    }
    
    public void setSummary(String summary) {
        this.summary = summary;
    }
    
    public String getSourceType() {
        return sourceType;
    }
    
    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }
    
    public List<Float> getEmbedding() {
        return embedding;
    }
    
    public void setEmbedding(List<Float> embedding) {
        this.embedding = embedding;
    }
}
