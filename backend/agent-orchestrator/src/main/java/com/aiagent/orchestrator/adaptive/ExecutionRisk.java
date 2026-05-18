package com.aiagent.orchestrator.adaptive;

public class ExecutionRisk {
    private double riskScore;
    private String reason;
    
    public ExecutionRisk() {}
    
    public ExecutionRisk(double riskScore, String reason) {
        this.riskScore = riskScore;
        this.reason = reason;
    }
    
    public double getRiskScore() {
        return riskScore;
    }
    
    public void setRiskScore(double riskScore) {
        this.riskScore = riskScore;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
}
