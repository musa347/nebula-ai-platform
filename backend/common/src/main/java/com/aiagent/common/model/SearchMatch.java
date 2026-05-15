package com.aiagent.common.model;

public class SearchMatch {
    private String file;
    private Integer lineNumber;
    private String snippet;

    public SearchMatch() {}

    public SearchMatch(String file, Integer lineNumber, String snippet) {
        this.file = file;
        this.lineNumber = lineNumber;
        this.snippet = snippet;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }
}
