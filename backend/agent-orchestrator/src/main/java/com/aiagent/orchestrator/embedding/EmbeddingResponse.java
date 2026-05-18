package com.aiagent.orchestrator.embedding;

import java.util.List;

public class EmbeddingResponse {
    private String sourceId;
    private List<Float> embedding;
    
    public EmbeddingResponse() {}
    
    public EmbeddingResponse(String sourceId, List<Float> embedding) {
        this.sourceId = sourceId;
        this.embedding = embedding;
    }
    
    public String getSourceId() {
        return sourceId;
    }
    
    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }
    
    public List<Float> getEmbedding() {
        return embedding;
    }
    
    public void setEmbedding(List<Float> embedding) {
        this.embedding = embedding;
    }
}
