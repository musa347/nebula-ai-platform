package com.aiagent.common.dto;

import com.aiagent.common.model.SymbolMatch;

import java.util.List;

public class SymbolSearchResponse {
    private List<SymbolMatch> matches;

    public SymbolSearchResponse() {}

    public SymbolSearchResponse(List<SymbolMatch> matches) {
        this.matches = matches;
    }

    public List<SymbolMatch> getMatches() {
        return matches;
    }

    public void setMatches(List<SymbolMatch> matches) {
        this.matches = matches;
    }
}
