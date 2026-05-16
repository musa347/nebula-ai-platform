package com.aiagent.orchestrator.service;

import com.aiagent.common.dto.OrchestratorTaskRequest;
import com.aiagent.common.dto.OrchestratorTaskResponse;
import com.aiagent.common.enums.ExecutionState;
import com.aiagent.common.model.LoadedContext;
import com.aiagent.common.model.FilePreview;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MinimalOrchestratorServiceTest {

    private MinimalOrchestratorService minimalOrchestratorService;
    private ExecutionSessionService executionSessionService;
    private StateTransitionService stateTransitionService;
    private ContextLoaderService contextLoaderService;
    private FileReaderService fileReaderService;

    @BeforeEach
    void setUp() {
        stateTransitionService = new StateTransitionService();
        executionSessionService = new ExecutionSessionService();
        contextLoaderService = new ContextLoaderService();
        fileReaderService = new FileReaderService();
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
            
            java.lang.reflect.Field orchestratorFileField = MinimalOrchestratorService.class.getDeclaredField("fileReaderService");
            orchestratorFileField.setAccessible(true);
            orchestratorFileField.set(minimalOrchestratorService, fileReaderService);
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
    void testFilePreviewLoaded_PreviewsExist() {
        OrchestratorTaskRequest request = new OrchestratorTaskRequest("Add caching to UserService");
        
        OrchestratorTaskResponse response = minimalOrchestratorService.execute(request);
        
        assertNotNull(response.getPreviews());
        assertFalse(response.getPreviews().isEmpty());
        
        // Verify previews contain expected files based on contexts
        List<String> previewFiles = response.getPreviews().stream()
            .map(FilePreview::getFile)
            .toList();
        
        assertTrue(previewFiles.contains("UserService.java"));
        assertTrue(previewFiles.contains("CacheService.java"));
    }

    @Test
    void testPreviewTruncated_NotFullFile() {
        OrchestratorTaskRequest request = new OrchestratorTaskRequest("Analyze UserService");
        
        OrchestratorTaskResponse response = minimalOrchestratorService.execute(request);
        
        assertNotNull(response.getPreviews());
        assertFalse(response.getPreviews().isEmpty());
        
        // Verify previews are truncated (not huge)
        for (FilePreview preview : response.getPreviews()) {
            assertNotNull(preview.getPreview());
            // Preview should be reasonable size (not full file)
            assertTrue(preview.getPreview().length() < 2000, "Preview should be truncated");
            assertTrue(preview.getPreview().length() > 0, "Preview should not be empty");
        }
    }

    @Test
    void testMissingFileSafe_ExecutionContinues() {
        // This test verifies that missing files don't break execution
        OrchestratorTaskRequest request = new OrchestratorTaskRequest("nonexistent file task");
        
        OrchestratorTaskResponse response = minimalOrchestratorService.execute(request);
        
        // Execution should complete successfully even if files are missing
        assertTrue(response.getCompletedStates().contains(ExecutionState.COMPLETED));
        assertNotNull(response.getPreviews());
    }

    @Test
    void testEmptyContexts_EmptyPreviewsList() {
        OrchestratorTaskRequest request = new OrchestratorTaskRequest("");
        
        OrchestratorTaskResponse response = minimalOrchestratorService.execute(request);
        
        assertNotNull(response.getPreviews());
        // Empty contexts should result in empty previews
        assertTrue(response.getPreviews().isEmpty());
        // But execution should still complete
        assertTrue(response.getCompletedStates().contains(ExecutionState.COMPLETED));
    }

    @Test
    void testStateFlowStillWorks_ExecutingTransitionsCorrectly() {
        OrchestratorTaskRequest request = new OrchestratorTaskRequest("Test execution flow");
        
        OrchestratorTaskResponse response = minimalOrchestratorService.execute(request);
        
        // Verify EXECUTING state is in the flow
        assertTrue(response.getCompletedStates().contains(ExecutionState.EXECUTING));
        
        // Verify state transitions are still working correctly
        List<ExecutionState> states = response.getCompletedStates();
        int executingIndex = states.indexOf(ExecutionState.EXECUTING);
        int contextLoadingIndex = states.indexOf(ExecutionState.CONTEXT_LOADING);
        int verifyingIndex = states.indexOf(ExecutionState.VERIFYING);
        
        assertTrue(contextLoadingIndex < executingIndex, "CONTEXT_LOADING should come before EXECUTING");
        assertTrue(executingIndex < verifyingIndex, "EXECUTING should come before VERIFYING");
    }
}