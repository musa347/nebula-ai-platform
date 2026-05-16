package com.aiagent.orchestrator.service;

import com.aiagent.common.dto.OrchestratorTaskRequest;
import com.aiagent.common.dto.OrchestratorTaskResponse;
import com.aiagent.common.enums.ExecutionState;
import com.aiagent.common.model.LoadedContext;
import com.aiagent.common.model.FilePreview;
import com.aiagent.common.model.PatchProposal;
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
    private PatchProposalService patchProposalService;

    @BeforeEach
    void setUp() {
        stateTransitionService = new StateTransitionService();
        executionSessionService = new ExecutionSessionService();
        contextLoaderService = new ContextLoaderService();
        fileReaderService = new FileReaderService();
        patchProposalService = new PatchProposalService();
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
            
            java.lang.reflect.Field orchestratorPatchField = MinimalOrchestratorService.class.getDeclaredField("patchProposalService");
            orchestratorPatchField.setAccessible(true);
            orchestratorPatchField.set(minimalOrchestratorService, patchProposalService);
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
    void testPatchGenerated_AtLeastOnePatchForCacheTask() {
        OrchestratorTaskRequest request = new OrchestratorTaskRequest("Add caching to UserService");
        
        OrchestratorTaskResponse response = minimalOrchestratorService.execute(request);
        
        assertNotNull(response.getPatches());
        assertFalse(response.getPatches().isEmpty());
        
        // Verify at least one patch is generated for caching task
        boolean hasCachingPatch = response.getPatches().stream()
            .anyMatch(patch -> patch.getDescription().toLowerCase().contains("cach"));
        assertTrue(hasCachingPatch, "Should generate at least one caching-related patch");
    }

    @Test
    void testEmptyTaskSafe_NoCrash() {
        OrchestratorTaskRequest request = new OrchestratorTaskRequest("");
        
        OrchestratorTaskResponse response = minimalOrchestratorService.execute(request);
        
        assertNotNull(response.getPatches());
        // Empty task should still complete successfully
        assertTrue(response.getCompletedStates().contains(ExecutionState.COMPLETED));
    }

    @Test
    void testNoContextSafe_ReturnsEmptyList() {
        OrchestratorTaskRequest request = new OrchestratorTaskRequest("xyz123nonexistent");
        
        OrchestratorTaskResponse response = minimalOrchestratorService.execute(request);
        
        assertNotNull(response.getPatches());
        // Should complete successfully even with minimal context
        assertTrue(response.getCompletedStates().contains(ExecutionState.COMPLETED));
    }

    @Test
    void testDeterministicOutput_SameInputSameOutput() {
        OrchestratorTaskRequest request = new OrchestratorTaskRequest("Add caching to UserService");
        
        OrchestratorTaskResponse response1 = minimalOrchestratorService.execute(request);
        OrchestratorTaskResponse response2 = minimalOrchestratorService.execute(request);
        
        // Same input should produce same number of patches
        assertEquals(response1.getPatches().size(), response2.getPatches().size());
        
        // Verify patch content is deterministic
        if (!response1.getPatches().isEmpty() && !response2.getPatches().isEmpty()) {
            PatchProposal patch1 = response1.getPatches().get(0);
            PatchProposal patch2 = response2.getPatches().get(0);
            assertEquals(patch1.getDescription(), patch2.getDescription());
            assertEquals(patch1.getSuggestedChange(), patch2.getSuggestedChange());
        }
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
    void testStateFlowStillWorks_VerifyingTransitionsCorrectly() {
        OrchestratorTaskRequest request = new OrchestratorTaskRequest("Test execution flow");
        
        OrchestratorTaskResponse response = minimalOrchestratorService.execute(request);
        
        // Verify VERIFYING state is in the flow
        assertTrue(response.getCompletedStates().contains(ExecutionState.VERIFYING));
        
        // Verify state transitions are still working correctly
        List<ExecutionState> states = response.getCompletedStates();
        int executingIndex = states.indexOf(ExecutionState.EXECUTING);
        int verifyingIndex = states.indexOf(ExecutionState.VERIFYING);
        int completedIndex = states.indexOf(ExecutionState.COMPLETED);
        
        assertTrue(executingIndex < verifyingIndex, "EXECUTING should come before VERIFYING");
        assertTrue(verifyingIndex < completedIndex, "VERIFYING should come before COMPLETED");
    }
}