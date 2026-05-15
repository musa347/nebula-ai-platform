package com.aiagent.orchestrator.retry;

import com.aiagent.common.dto.ExecutionEvent;
import com.aiagent.common.dto.ExecutionEventType;
import com.aiagent.common.enums.AgentState;
import com.aiagent.common.enums.FailureType;
import com.aiagent.orchestrator.actions.EmbabelActions;
import com.aiagent.orchestrator.service.ExecutionGraphService;
import com.aiagent.orchestrator.trace.ExecutionTrace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.UUID;
import java.util.function.Supplier;

@Service
public class RetryOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(RetryOrchestrator.class);

    private final EmbabelActions actions;
    private final FailureClassifier classifier;
    private final ExecutionGraphService executionGraphService;

    public RetryOrchestrator(EmbabelActions actions, FailureClassifier classifier, ExecutionGraphService executionGraphService) {
        this.actions = actions;
        this.classifier = classifier;
        this.executionGraphService = executionGraphService;
    }

    public Flux<ExecutionEvent> executeWithRetry(String command, RetryPolicy policy) {
        String executionId = UUID.randomUUID().toString();
        Sinks.Many<ExecutionEvent> sink = Sinks.many().multicast().onBackpressureBuffer();
        ExecutionTrace trace = new ExecutionTrace(executionId);
        
        executionGraphService.create(executionId);

        Thread.ofVirtual().start(() -> {
            try {
                runLoop(executionId, command, policy, trace, sink);
            } finally {
                trace.finish();
                sink.tryEmitComplete();
            }
        });

        return sink.asFlux();
    }

    private void runLoop(String executionId, String command, RetryPolicy policy,
                         ExecutionTrace trace, Sinks.Many<ExecutionEvent> sink) {
        long deadline = System.currentTimeMillis() + policy.getMaxExecutionDurationMs();
        AgentState state = AgentState.IDLE;
        int attempt = 0;

        while (attempt <= policy.getMaxRetries()) {
            if (System.currentTimeMillis() > deadline) {
                emit(sink, executionId, ExecutionEventType.PROCESS_TIMEOUT,
                        "Max execution duration exceeded");
                transitionState(trace, sink, executionId, state, AgentState.FAILED);
                return;
            }

            if (attempt > 0) {
                transitionState(trace, sink, executionId, state, AgentState.RETRYING);
                state = AgentState.RETRYING;
                executionGraphService.addNode(executionId, "RETRY", "Retry attempt " + attempt);
                emit(sink, executionId, ExecutionEventType.RETRY_STARTED,
                        "Retry attempt " + attempt + " of " + policy.getMaxRetries());
                sleep(policy.backoffDelayMs(attempt - 1));
            }

            transitionState(trace, sink, executionId, state, AgentState.RUNNING_COMMAND);
            state = AgentState.RUNNING_COMMAND;

            String output = actions.executeShell(command);
            trace.recordToolCall("shell.execute", command, output);

            if (isSuccess(output)) {
                transitionState(trace, sink, executionId, state, AgentState.DONE);
                emit(sink, executionId, ExecutionEventType.RETRY_COMPLETED,
                        "Execution succeeded" + (attempt > 0 ? " after " + attempt + " retries" : ""));
                return;
            }

            // Failure path
            transitionState(trace, sink, executionId, state, AgentState.ANALYZING_FAILURE);
            state = AgentState.ANALYZING_FAILURE;

            FailureType failureType = classifier.classify(output);
            trace.recordRetryAttempt(attempt, failureType, output);

            emit(sink, executionId, ExecutionEventType.FAILURE_ANALYZED,
                    "Failure classified as: " + failureType + " | retryable: " + failureType.isRetryable());

            if (!failureType.isRetryable()) {
                transitionState(trace, sink, executionId, state, AgentState.FAILED);
                executionGraphService.addNode(executionId, "FAILURE", "Non-retryable: " + failureType);
                emit(sink, executionId, ExecutionEventType.PROCESS_FAILED,
                        "Non-retryable failure: " + failureType);
                return;
            }

            attempt++;
        }

        transitionState(trace, sink, executionId, state, AgentState.FAILED);
        emit(sink, executionId, ExecutionEventType.PROCESS_FAILED,
                "Max retries (" + policy.getMaxRetries() + ") exhausted");
    }

    private void transitionState(ExecutionTrace trace, Sinks.Many<ExecutionEvent> sink,
                                  String executionId, AgentState from, AgentState to) {
        trace.recordStateTransition(from, to);
        emit(sink, executionId, ExecutionEventType.STATE_CHANGED, from + " → " + to);
        log.debug("[{}] State: {} → {}", executionId, from, to);
    }

    private void emit(Sinks.Many<ExecutionEvent> sink, String executionId,
                      ExecutionEventType type, String message) {
        sink.tryEmitNext(ExecutionEvent.builder()
                .executionId(executionId)
                .type(type)
                .message(message)
                .build());
    }

    private boolean isSuccess(String output) {
        return output != null && output.equals("SUCCESS");
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
