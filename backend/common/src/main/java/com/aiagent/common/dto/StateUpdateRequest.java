package com.aiagent.common.dto;

import com.aiagent.common.enums.ExecutionState;

public class StateUpdateRequest {
    private ExecutionState state;

    public StateUpdateRequest() {}

    public StateUpdateRequest(ExecutionState state) {
        this.state = state;
    }

    public ExecutionState getState() {
        return state;
    }

    public void setState(ExecutionState state) {
        this.state = state;
    }
}