package com.aiagent.orchestrator.service;

import com.aiagent.common.dto.OrchestratorTaskRequest;
import com.aiagent.common.dto.OrchestratorTaskResponse;
import com.aiagent.common.enums.ExecutionState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MinimalOrchestratorServiceTest {

    private MinimalOrchestratorService minimalOrchestratorService;
    private ExecutionSessionService executionSessionService;
    private StateTransitionService stateTransitionService;

    @BeforeEach
    void setUp() {
        stateTransitionService = new StateTransitionService();
        executionSessionService = new ExecutionSessionService();
        minimalOrchestratorService = new MinimalOrchestratorService();
        
        // Inject dependencies using reflection
        try {
            java.lang.reflect.Field sessionField = ExecutionSessionService.class.getDeclaredField("stateTransitionService");
            sessionField.setAccessible(true);
            sessionField.set(executionSessionService, stateTransitionService);
            
            java.lang.reflect.Field orchestratorField = MinimalOrchestratorService.class.getDeclaredField("executionSessionService");
            orchestratorField.setAccessible(true);
            orchestratorField.set(minimalOrchestratorService, executionSessionService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testFullSuccessfulFlow_StatesExecuteInOrder() {
        OrchestratorTaskRequest request = new OrchestratorTaskRequest("Analyze UserService");
        
        OrchestratorTaskResponse response = minimalOrchestratorService.execute(request);
        
        List<ExecutionState> expectedStates = List.of(
            ExecutionState.CREATED,
            ExecutionState.PLANNING,
            ExecutionState.CONTEXT_LOADING,
            ExecutionState.EXECUTING,
            ExecutionState.VERIFYING,
            ExecutionState.COMPLETED
        );
        
        assertEquals(expectedStates, response.getCompletedStates());
    }

    @Test
    void testSessionCreated_ExecutionIdExists() {
        OrchestratorTaskRequest request = new OrchestratorTaskRequest("Test task");
        
        OrchestratorTaskResponse response = minimalOrchestratorService.execute(request);
        
        assertNotNull(response.getExecutionId());
        assertFalse(response.getExecutionId().isEmpty());
    }

    @Test
    void testInvalidStateHandling_ForceFailure() {
        // Create a custom orchestrator service that forces an invalid transition
        MinimalOrchestratorService faultyService = new MinimalOrchestratorService() {
            @Override
            public OrchestratorTaskResponse execute(OrchestratorTaskRequest request) {
                // Create session normally
                ExecutionSessionService sessionService = getExecutionSessionService();
                var session = sessionService.create();
                
                // Force session to COMPLETED state to make next transition invalid
                sessionService.updateState(session.getExecutionId(), ExecutionState.PLANNING);
                sessionService.updateState(session.getExecutionId(), ExecutionState.CONTEXT_LOADING);
                sessionService.updateState(session.getExecutionId(), ExecutionState.EXECUTING);
                sessionService.updateState(session.getExecutionId(), ExecutionState.VERIFYING);
                sessionService.updateState(session.getExecutionId(), ExecutionState.COMPLETED);
                
                // Now try to transition to EXECUTING (invalid from COMPLETED)
                var result = sessionService.updateState(session.getExecutionId(), ExecutionState.EXECUTING);
                
                if (!result.isAllowed()) {
                    sessionService.updateState(session.getExecutionId(), ExecutionState.FAILED);
                    return new OrchestratorTaskResponse(session.getExecutionId(), 
                        List.of(ExecutionState.CREATED, ExecutionState.PLANNING, 
                               ExecutionState.CONTEXT_LOADING, ExecutionState.EXECUTING, 
                               ExecutionState.VERIFYING, ExecutionState.COMPLETED, 
                               ExecutionState.FAILED));
                }
                
                return new OrchestratorTaskResponse(session.getExecutionId(), List.of());
            }
            
            private ExecutionSessionService getExecutionSessionService() {
                return executionSessionService;
            }
        };
        
        // Inject dependency
        try {
            java.lang.reflect.Field field = MinimalOrchestratorService.class.getDeclaredField("executionSessionService");
            field.setAccessible(true);
            field.set(faultyService, executionSessionService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        OrchestratorTaskRequest request = new OrchestratorTaskRequest("Test task");
        OrchestratorTaskResponse response = faultyService.execute(request);
        
        assertTrue(response.getCompletedStates().contains(ExecutionState.FAILED));
    }
}