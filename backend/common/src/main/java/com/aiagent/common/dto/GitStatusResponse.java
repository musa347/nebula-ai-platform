package com.aiagent.common.dto;

import java.util.List;

public class GitStatusResponse {

    private List<String> modifiedFiles;
    private List<String> untrackedFiles;
    private String currentBranch;

    public GitStatusResponse() {}

    public GitStatusResponse(List<String> modifiedFiles, List<String> untrackedFiles, String currentBranch) {
        this.modifiedFiles = modifiedFiles;
        this.untrackedFiles = untrackedFiles;
        this.currentBranch = currentBranch;
    }

    public List<String> getModifiedFiles() { return modifiedFiles; }
    public void setModifiedFiles(List<String> modifiedFiles) { this.modifiedFiles = modifiedFiles; }

    public List<String> getUntrackedFiles() { return untrackedFiles; }
    public void setUntrackedFiles(List<String> untrackedFiles) { this.untrackedFiles = untrackedFiles; }

    public String getCurrentBranch() { return currentBranch; }
    public void setCurrentBranch(String currentBranch) { this.currentBranch = currentBranch; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<String> modifiedFiles;
        private List<String> untrackedFiles;
        private String currentBranch;

        public Builder modifiedFiles(List<String> modifiedFiles) {
            this.modifiedFiles = modifiedFiles;
            return this;
        }

        public Builder untrackedFiles(List<String> untrackedFiles) {
            this.untrackedFiles = untrackedFiles;
            return this;
        }

        public Builder currentBranch(String currentBranch) {
            this.currentBranch = currentBranch;
            return this;
        }

        public GitStatusResponse build() {
            return new GitStatusResponse(modifiedFiles, untrackedFiles, currentBranch);
        }
    }
}
