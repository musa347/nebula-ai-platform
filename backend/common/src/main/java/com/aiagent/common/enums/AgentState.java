package com.aiagent.common.enums;

public enum AgentState {
    IDLE,
    PLANNING,
    READING_CONTEXT,
    GENERATING_PATCH,
    APPLYING_PATCH,
    RUNNING_COMMAND,
    STREAMING_OUTPUT,
    ANALYZING_FAILURE,
    RETRYING,
    VERIFYING,
    DONE,
    FAILED
}
