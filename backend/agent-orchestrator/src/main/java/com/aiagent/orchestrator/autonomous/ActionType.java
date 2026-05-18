package com.aiagent.orchestrator.autonomous;

public enum ActionType {
    PLAN,
    EXECUTE_TOOL,
    GENERATE_PATCH,
    APPLY_PATCH,
    RETRY,
    ANALYZE_FAILURE,
    COMPLETE
}
