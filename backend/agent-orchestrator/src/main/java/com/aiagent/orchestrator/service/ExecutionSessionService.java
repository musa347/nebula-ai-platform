package com.aiagent.orchestrator.service;

import com.aiagent.common.enums.ExecutionState;
import com.aiagent.common.model.ExecutionSession;
import com.aiagent.common.model.TransitionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ExecutionSessionService {

    private final ConcurrentHashMap<String, ExecutionSession> sessions = new ConcurrentHashMap<>();

    @Autowired
    private StateTransitionService stateTransitionService;

    public ExecutionSession create() {
        String executionId = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();
        
        ExecutionSession session = new ExecutionSession(
            executionId,
            ExecutionState.CREATED,
            now,
            now
        );
        
        sessions.put(executionId, session);
        return session;
    }

    public ExecutionSession get(String executionId) {
        return sessions.get(executionId);
    }

    public TransitionResult updateState(String executionId, ExecutionState newState) {
        ExecutionSession session = sessions.get(executionId);
        if (session == null) {
            return new TransitionResult(false, "Session not found");
        }

        TransitionResult transitionResult = stateTransitionService.canTransition(
            session.getCurrentState(), 
            newState
        );

        if (transitionResult.isAllowed()) {
            session.setCurrentState(newState);
            session.setUpdatedAt(System.currentTimeMillis());
        }

        return transitionResult;
    }
}