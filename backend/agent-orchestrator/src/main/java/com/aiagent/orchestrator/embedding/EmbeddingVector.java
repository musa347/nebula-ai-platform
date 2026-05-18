package com.aiagent.orchestrator.embedding;

import java.util.List;

public class EmbeddingVector {
    private String id;
    private List<Float> values;
    private String sourceType;
    
    public EmbeddingVector() {}
    
    public EmbeddingVector(String id, List<Float> values, String sourceType) {
        this.id = id;
        this.values = values;
        this.sourceType = sourceType;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public List<Float> getValues() {
        return values;
    }
    
    public void setValues(List<Float> values) {
        this.values = values;
    }
    
    public String getSourceType() {
        return sourceType;
    }
    
    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }
}
