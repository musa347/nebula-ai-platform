package com.aiagent.common.dto;

import com.aiagent.common.model.SearchMatch;

import java.util.List;

public class RepoGrepResponse {
    private List<SearchMatch> matches;

    public RepoGrepResponse() {}

    public RepoGrepResponse(List<SearchMatch> matches) {
        this.matches = matches;
    }

    public List<SearchMatch> getMatches() {
        return matches;
    }

    public void setMatches(List<SearchMatch> matches) {
        this.matches = matches;
    }
}
