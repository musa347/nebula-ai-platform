package com.aiagent.common.dto;

public enum ExecutionEventType {

    PROCESS_STARTED,
    STDOUT,
    STDERR,
    PROCESS_EXIT,
    PROCESS_TIMEOUT,
    PROCESS_FAILED,

    // Retry lifecycle events
    RETRY_STARTED,
    RETRY_COMPLETED,
    FAILURE_ANALYZED,
    PATCH_GENERATED,
    PATCH_APPLIED,
    STATE_CHANGED
}
