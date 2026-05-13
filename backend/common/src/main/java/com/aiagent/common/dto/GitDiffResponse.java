package com.aiagent.common.dto;

import java.util.List;

public class GitDiffResponse {

    private String diff;
    private List<String> affectedFiles;

    public GitDiffResponse() {}

    public GitDiffResponse(String diff, List<String> affectedFiles) {
        this.diff = diff;
        this.affectedFiles = affectedFiles;
    }

    public String getDiff() { return diff; }
    public void setDiff(String diff) { this.diff = diff; }

    public List<String> getAffectedFiles() { return affectedFiles; }
    public void setAffectedFiles(List<String> affectedFiles) { this.affectedFiles = affectedFiles; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String diff;
        private List<String> affectedFiles;

        public Builder diff(String diff) {
            this.diff = diff;
            return this;
        }

        public Builder affectedFiles(List<String> affectedFiles) {
            this.affectedFiles = affectedFiles;
            return this;
        }

        public GitDiffResponse build() {
            return new GitDiffResponse(diff, affectedFiles);
        }
    }
}
