package com.aiagent.orchestrator.model;

public enum RecoveryAction {
    REGENERATE_PATCH,
    RETRY_EXECUTION,
    RELOAD_CONTEXT,
    REDUCE_SCOPE,
    FAIL_SAFE
}
