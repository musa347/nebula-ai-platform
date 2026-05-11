package com.aiagent.mcp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response object for filesystem patch operations.
 */
public class PatchResponse {
    
    private boolean success;
    private String diff;
    @JsonProperty("backupCreated")
    private boolean backupCreated;
    private String message;
    private String backupPath;
    
    public PatchResponse() {}
    
    public PatchResponse(boolean success, String diff, boolean backupCreated, String message, String backupPath) {
        this.success = success;
        this.diff = diff;
        this.backupCreated = backupCreated;
        this.message = message;
        this.backupPath = backupPath;
    }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getDiff() { return diff; }
    public void setDiff(String diff) { this.diff = diff; }
    
    public boolean isBackupCreated() { return backupCreated; }
    public void setBackupCreated(boolean backupCreated) { this.backupCreated = backupCreated; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getBackupPath() { return backupPath; }
    public void setBackupPath(String backupPath) { this.backupPath = backupPath; }
    
    public static PatchResponse success(String diff, boolean backupCreated, String backupPath) {
        return new PatchResponse(true, diff, backupCreated, "Patch applied successfully", backupPath);
    }
    
    public static PatchResponse failure(String message) {
        return new PatchResponse(false, null, false, message, null);
    }
}
