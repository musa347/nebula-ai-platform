package com.aiagent.orchestrator.context;

public class RelatedContext {
    private String file;
    private String relationType;
    
    public RelatedContext() {}
    
    public RelatedContext(String file, String relationType) {
        this.file = file;
        this.relationType = relationType;
    }
    
    public String getFile() {
        return file;
    }
    
    public void setFile(String file) {
        this.file = file;
    }
    
    public String getRelationType() {
        return relationType;
    }
    
    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }
}
