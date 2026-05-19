package com.aiagent.orchestrator.streaming;

public enum ExecutionEventType {
    PLAN_CREATED,
    MEMORY_MATCHED,
    RISK_ANALYZED,
    TOOL_SELECTED,
    TOOL_EXECUTED,
    PATCH_GENERATED,
    PATCH_APPLIED,
    LEARNING_UPDATED,
    ERROR,
    COMPLETE
}
