package com.aiagent.orchestrator.service;

import com.aiagent.common.dto.OrchestratorTaskRequest;
import com.aiagent.common.dto.OrchestratorTaskResponse;
import com.aiagent.common.enums.ExecutionState;
import com.aiagent.common.model.LoadedContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MinimalOrchestratorServiceTest {

    private MinimalOrchestratorService minimalOrchestratorService;
    private ExecutionSessionService executionSessionService;
    private StateTransitionService stateTransitionService;
    private ContextLoaderService contextLoaderService;

    @BeforeEach
    void setUp() {
        stateTransitionService = new StateTransitionService();
        executionSessionService = new ExecutionSessionService();
        contextLoaderService = new ContextLoaderService();
        minimalOrchestratorService = new MinimalOrchestratorService();
        
        // Inject dependencies using reflection
        try {
            java.lang.reflect.Field sessionField = ExecutionSessionService.class.getDeclaredField("stateTransitionService");
            sessionField.setAccessible(true);
            sessionField.set(executionSessionService, stateTransitionService);
            
            java.lang.reflect.Field orchestratorSessionField = MinimalOrchestratorService.class.getDeclaredField("executionSessionService");
            orchestratorSessionField.setAccessible(true);
            orchestratorSessionField.set(minimalOrchestratorService, executionSessionService);
            
            java.lang.reflect.Field orchestratorContextField = MinimalOrchestratorService.class.getDeclaredField("contextLoaderService");
            orchestratorContextField.setAccessible(true);
            orchestratorContextField.set(minimalOrchestratorService, contextLoaderService);
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
    void testContextsLoaded_ResponseContainsContexts() {
        OrchestratorTaskRequest request = new OrchestratorTaskRequest("Add caching to UserService");
        
        OrchestratorTaskResponse response = minimalOrchestratorService.execute(request);
        
        assertNotNull(response.getContexts());
        assertFalse(response.getContexts().isEmpty());
        
        // Verify context contains expected files based on task
        List<String> contextFiles = response.getContexts().stream()
            .map(LoadedContext::getFile)
            .toList();
        
        assertTrue(contextFiles.contains("UserService.java"));
        assertTrue(contextFiles.contains("CacheService.java"));
    }

    @Test
    void testEmptyTask_SafeHandling() {
        OrchestratorTaskRequest request = new OrchestratorTaskRequest("");
        
        OrchestratorTaskResponse response = minimalOrchestratorService.execute(request);
        
        assertNotNull(response.getContexts());
        // Empty task should still complete successfully
        assertTrue(response.getCompletedStates().contains(ExecutionState.COMPLETED));
    }

    @Test
    void testContextStateFlow_ContextLoadingOccursCorrectly() {
        OrchestratorTaskRequest request = new OrchestratorTaskRequest("Test context loading");
        
        OrchestratorTaskResponse response = minimalOrchestratorService.execute(request);
        
        // Verify CONTEXT_LOADING state is in the flow
        assertTrue(response.getCompletedStates().contains(ExecutionState.CONTEXT_LOADING));
        
        // Verify contexts are loaded
        assertNotNull(response.getContexts());
    }

    @Test
    void testNoMatches_ReturnsEmptyContextsSafely() {
        OrchestratorTaskRequest request = new OrchestratorTaskRequest("xyz123nonexistent");
        
        OrchestratorTaskResponse response = minimalOrchestratorService.execute(request);
        
        assertNotNull(response.getContexts());
        // Should still have default context
        assertFalse(response.getContexts().isEmpty());
        assertTrue(response.getCompletedStates().contains(ExecutionState.COMPLETED));
    }
}