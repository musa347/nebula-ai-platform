package com.aiagent.orchestrator.context;

import java.util.List;

public class ImpactAnalysis {
    private List<String> affectedFiles;
    private List<String> affectedClasses;
    private String riskLevel;
    
    public ImpactAnalysis() {}
    
    public ImpactAnalysis(List<String> affectedFiles, List<String> affectedClasses, String riskLevel) {
        this.affectedFiles = affectedFiles;
        this.affectedClasses = affectedClasses;
        this.riskLevel = riskLevel;
    }
    
    public List<String> getAffectedFiles() {
        return affectedFiles;
    }
    
    public void setAffectedFiles(List<String> affectedFiles) {
        this.affectedFiles = affectedFiles;
    }
    
    public List<String> getAffectedClasses() {
        return affectedClasses;
    }
    
    public void setAffectedClasses(List<String> affectedClasses) {
        this.affectedClasses = affectedClasses;
    }
    
    public String getRiskLevel() {
        return riskLevel;
    }
    
    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }
}
