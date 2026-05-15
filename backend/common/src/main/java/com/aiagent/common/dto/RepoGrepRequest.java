package com.aiagent.common.dto;

public class RepoGrepRequest {
    private String query;

    public RepoGrepRequest() {}

    public RepoGrepRequest(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
