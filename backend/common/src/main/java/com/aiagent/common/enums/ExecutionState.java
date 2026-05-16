package com.aiagent.common.enums;

public enum ExecutionState {
    CREATED,
    PLANNING,
    CONTEXT_LOADING,
    EXECUTING,
    VERIFYING,
    RETRYING,
    COMPLETED,
    FAILED
}