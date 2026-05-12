package com.aiagent.common.enums;

public enum FailureType {
    TEST_FAILURE,
    COMPILATION_FAILURE,
    MISSING_IMPORT,
    ASSERTION_FAILURE,
    SECURITY_VIOLATION,
    INVALID_COMMAND,
    TIMEOUT,
    UNKNOWN;

    public boolean isRetryable() {
        return switch (this) {
            case TEST_FAILURE, COMPILATION_FAILURE, MISSING_IMPORT, ASSERTION_FAILURE -> true;
            case SECURITY_VIOLATION, INVALID_COMMAND, TIMEOUT, UNKNOWN -> false;
        };
    }
}
