package com.aiagent.orchestrator.service;

import com.aiagent.common.enums.ExecutionState;
import com.aiagent.common.model.TransitionResult;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.Map;

@Service
public class StateTransitionService {

    private static final Map<ExecutionState, Set<ExecutionState>> ALLOWED_TRANSITIONS = Map.of(
        ExecutionState.CREATED, Set.of(ExecutionState.PLANNING),
        ExecutionState.PLANNING, Set.of(ExecutionState.CONTEXT_LOADING),
        ExecutionState.CONTEXT_LOADING, Set.of(ExecutionState.EXECUTING),
        ExecutionState.EXECUTING, Set.of(ExecutionState.VERIFYING),
        ExecutionState.VERIFYING, Set.of(ExecutionState.PATCH_APPLYING, ExecutionState.RETRYING),
        ExecutionState.PATCH_APPLYING, Set.of(ExecutionState.COMPLETED),
        ExecutionState.RETRYING, Set.of(ExecutionState.EXECUTING)
    );

    public TransitionResult canTransition(ExecutionState from, ExecutionState to) {
        if (from == null || to == null) {
            return new TransitionResult(false, "States cannot be null");
        }

        // Any state can transition to FAILED
        if (to == ExecutionState.FAILED) {
            return new TransitionResult(true, "Transition to FAILED allowed from any state");
        }

        Set<ExecutionState> allowedStates = ALLOWED_TRANSITIONS.get(from);
        if (allowedStates == null || !allowedStates.contains(to)) {
            return new TransitionResult(false, "Invalid transition from " + from + " to " + to);
        }

        return new TransitionResult(true, "Valid transition");
    }
}