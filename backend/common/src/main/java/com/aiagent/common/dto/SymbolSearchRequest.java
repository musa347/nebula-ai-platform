package com.aiagent.common.dto;

public class SymbolSearchRequest {
    private String query;

    public SymbolSearchRequest() {}

    public SymbolSearchRequest(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
