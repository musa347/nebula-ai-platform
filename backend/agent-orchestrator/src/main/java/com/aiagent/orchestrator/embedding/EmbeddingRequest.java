package com.aiagent.orchestrator.embedding;

public class EmbeddingRequest {
    private String content;
    private String sourceId;
    
    public EmbeddingRequest() {}
    
    public EmbeddingRequest(String content, String sourceId) {
        this.content = content;
        this.sourceId = sourceId;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getSourceId() {
        return sourceId;
    }
    
    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }
}
