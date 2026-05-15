package com.aiagent.common.dto;

import java.util.List;

public class RepoSearchResponse {
    private List<String> files;

    public RepoSearchResponse() {}

    public RepoSearchResponse(List<String> files) {
        this.files = files;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }
}
