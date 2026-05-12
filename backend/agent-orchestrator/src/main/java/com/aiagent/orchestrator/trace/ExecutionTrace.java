package com.aiagent.orchestrator.trace;

import com.aiagent.common.enums.AgentState;
import com.aiagent.common.enums.FailureType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ExecutionTrace {

    private final String executionId;
    private final long startedAt = Instant.now().toEpochMilli();
    private long finishedAt;

    private final List<StateTransition> stateTransitions = new ArrayList<>();
    private final List<ToolCall> toolCalls = new ArrayList<>();
    private final List<RetryAttempt> retryAttempts = new ArrayList<>();

    public ExecutionTrace(String executionId) {
        this.executionId = executionId;
    }

    public void recordStateTransition(AgentState from, AgentState to) {
        stateTransitions.add(new StateTransition(from, to, Instant.now().toEpochMilli()));
    }

    public void recordToolCall(String tool, String input, String output) {
        toolCalls.add(new ToolCall(tool, input, output, Instant.now().toEpochMilli()));
    }

    public void recordRetryAttempt(int attempt, FailureType failureType, String failureOutput) {
        retryAttempts.add(new RetryAttempt(attempt, failureType, failureOutput, Instant.now().toEpochMilli()));
    }

    public void finish() {
        this.finishedAt = Instant.now().toEpochMilli();
    }

    public String getExecutionId() { return executionId; }
    public long getStartedAt() { return startedAt; }
    public long getFinishedAt() { return finishedAt; }
    public List<StateTransition> getStateTransitions() { return stateTransitions; }
    public List<ToolCall> getToolCalls() { return toolCalls; }
    public List<RetryAttempt> getRetryAttempts() { return retryAttempts; }

    public record StateTransition(AgentState from, AgentState to, long timestamp) {}
    public record ToolCall(String tool, String input, String output, long timestamp) {}
    public record RetryAttempt(int attempt, FailureType failureType, String failureOutput, long timestamp) {}
}
