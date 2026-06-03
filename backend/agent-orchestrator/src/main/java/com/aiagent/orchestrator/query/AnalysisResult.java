package com.aiagent.orchestrator.query;

import java.util.ArrayList;
import java.util.List;

public class AnalysisResult {
    private String summary;
    private List<String> filesAnalyzed;
    private List<String> keyFindings;
    private String detailedExplanation;

    public AnalysisResult() {
        this.filesAnalyzed = new ArrayList<>();
        this.keyFindings = new ArrayList<>();
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getFilesAnalyzed() {
        return filesAnalyzed;
    }

    public void addFile(String file) {
        this.filesAnalyzed.add(file);
    }

    public List<String> getKeyFindings() {
        return keyFindings;
    }

    public void addFinding(String finding) {
        this.keyFindings.add(finding);
    }

    public String getDetailedExplanation() {
        return detailedExplanation;
    }

    public void setDetailedExplanation(String detailedExplanation) {
        this.detailedExplanation = detailedExplanation;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (!filesAnalyzed.isEmpty()) {
            sb.append("FILES_ANALYZED:");
            sb.append(String.join(", ", filesAnalyzed));
            sb.append("\n");
        }

        if (!keyFindings.isEmpty()) {
            sb.append("KEY_FINDINGS:");
            sb.append(String.join(", ", keyFindings));
            sb.append("\n");
        }

        if (detailedExplanation != null && !detailedExplanation.isEmpty()) {
            sb.append("EXPLANATION:");
            sb.append(detailedExplanation);
        }

        return sb.toString();
    }
}
