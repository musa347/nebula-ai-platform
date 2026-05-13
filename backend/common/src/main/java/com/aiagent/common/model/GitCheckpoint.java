package com.aiagent.common.model;

import java.time.LocalDateTime;
import java.util.List;

public class GitCheckpoint {
    
    private String checkpointId;
    private String sessionId;
    private String stashId;
    private List<String> modifiedFiles;
    private LocalDateTime createdAt;
    private String description;
    
    public GitCheckpoint() {}
    
    public GitCheckpoint(String checkpointId, String sessionId, String stashId, 
                        List<String> modifiedFiles, LocalDateTime createdAt, String description) {
        this.checkpointId = checkpointId;
        this.sessionId = sessionId;
        this.stashId = stashId;
        this.modifiedFiles = modifiedFiles;
        this.createdAt = createdAt;
        this.description = description;
    }
    
    public String getCheckpointId() { return checkpointId; }
    public void setCheckpointId(String checkpointId) { this.checkpointId = checkpointId; }
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public String getStashId() { return stashId; }
    public void setStashId(String stashId) { this.stashId = stashId; }
    
    public List<String> getModifiedFiles() { return modifiedFiles; }
    public void setModifiedFiles(List<String> modifiedFiles) { this.modifiedFiles = modifiedFiles; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String checkpointId;
        private String sessionId;
        private String stashId;
        private List<String> modifiedFiles;
        private LocalDateTime createdAt;
        private String description;
        
        public Builder checkpointId(String checkpointId) {
            this.checkpointId = checkpointId;
            return this;
        }
        
        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }
        
        public Builder stashId(String stashId) {
            this.stashId = stashId;
            return this;
        }
        
        public Builder modifiedFiles(List<String> modifiedFiles) {
            this.modifiedFiles = modifiedFiles;
            return this;
        }
        
        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public GitCheckpoint build() {
            return new GitCheckpoint(checkpointId, sessionId, stashId, modifiedFiles, createdAt, description);
        }
    }
}
