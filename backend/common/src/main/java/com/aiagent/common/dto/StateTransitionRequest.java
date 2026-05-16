package com.aiagent.common.dto;

import com.aiagent.common.enums.ExecutionState;

public class StateTransitionRequest {
    private ExecutionState from;
    private ExecutionState to;

    public StateTransitionRequest() {}

    public StateTransitionRequest(ExecutionState from, ExecutionState to) {
        this.from = from;
        this.to = to;
    }

    public ExecutionState getFrom() {
        return from;
    }

    public void setFrom(ExecutionState from) {
        this.from = from;
    }

    public ExecutionState getTo() {
        return to;
    }

    public void setTo(ExecutionState to) {
        this.to = to;
    }
}