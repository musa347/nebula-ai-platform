package com.aiagent.common.dto;

import java.util.List;

public class GitRestoreResponse {

    private List<String> restoredFiles;
    private boolean success;

    public GitRestoreResponse() {}

    public GitRestoreResponse(List<String> restoredFiles, boolean success) {
        this.restoredFiles = restoredFiles;
        this.success = success;
    }

    public List<String> getRestoredFiles() { return restoredFiles; }
    public void setRestoredFiles(List<String> restoredFiles) { this.restoredFiles = restoredFiles; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<String> restoredFiles;
        private boolean success;

        public Builder restoredFiles(List<String> restoredFiles) {
            this.restoredFiles = restoredFiles;
            return this;
        }

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public GitRestoreResponse build() {
            return new GitRestoreResponse(restoredFiles, success);
        }
    }
}
