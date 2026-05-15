package com.aiagent.common.dto;

public class RepoSearchRequest {
    private String query;

    public RepoSearchRequest() {}

    public RepoSearchRequest(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
