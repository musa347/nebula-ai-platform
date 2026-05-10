package com.aiagent.common.enums;


public enum ExecutionStatus {

    PENDING("pending"),
    RUNNING("running"),
    SUCCESS("success"),
    FAILED("failed"),
    TIMEOUT("timeout"),
    CANCELLED("cancelled"),
    SKIPPED("skipped");
    
    private final String status;
    
    ExecutionStatus(String status) {
        this.status = status;
    }
    
    public String getStatus() {
        return status;
    }

    public boolean isCompleted() {
        return this == SUCCESS || this == FAILED || this == TIMEOUT || this == CANCELLED || this == SKIPPED;
    }

    public boolean isSuccessful() {
        return this == SUCCESS;
    }

    public boolean isFailure() {
        return this == FAILED || this == TIMEOUT || this == CANCELLED;
    }

    public boolean isActive() {
        return this == PENDING || this == RUNNING;
    }
}
