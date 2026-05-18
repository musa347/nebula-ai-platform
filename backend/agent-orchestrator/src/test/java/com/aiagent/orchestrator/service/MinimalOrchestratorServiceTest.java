package com.aiagent.orchestrator.service;

import com.aiagent.common.dto.OrchestratorTaskRequest;
import com.aiagent.common.dto.OrchestratorTaskResponse;
import com.aiagent.common.enums.ExecutionState;
import com.aiagent.common.model.LoadedContext;
import com.aiagent.common.model.FilePreview;
import com.aiagent.common.model.PatchProposal;
import com.aiagent.common.model.PatchExecutionResult;
import com.aiagent.orchestrator.learning.ToolBiasService;
import com.aiagent.orchestrator.learning.LearningSignalStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MinimalOrchestratorServiceTest {

    private MinimalOrchestratorService minimalOrchestratorService;
    private ExecutionSessionService executionSessionService;
    private StateTransitionService stateTransitionService;
    private PlanningService planningService;
    private ToolRouterService toolRouterService;
    private ToolExecutionService toolExecutionService;
    private ExecutionStatsService executionStatsService;
    private ContextLoaderService contextLoaderService;
    private FileReaderService fileReaderService;
    private PatchProposalService patchProposalService;
    private PatchExecutionService patchExecutionService;
    private BackupService backupService;
    private PatchSafetyService patchSafetyService;
    private ToolBiasService toolBiasService;
    private com.aiagent.orchestrator.adaptive.AdaptivePlanningService adaptivePlanningService;
    private com.aiagent.orchestrator.adaptive.ExecutionRiskAnalyzer riskAnalyzer;
    private com.aiagent.orchestrator.adaptive.ToolStrategyEngine strategyEngine;

    @BeforeEach
    void setUp() {
        stateTransitionService = new StateTransitionService();
        executionSessionService = new ExecutionSessionService();
        executionStatsService = new ExecutionStatsService();
        LearningSignalStore learningSignalStore = new LearningSignalStore();
        toolBiasService = new ToolBiasService(learningSignalStore);
        
        // Create adaptive services with stub implementations
        com.aiagent.orchestrator.memory.SemanticMemoryStore memoryStore = new com.aiagent.orchestrator.memory.SemanticMemoryStore();
        com.aiagent.orchestrator.embedding.CosineSimilarityService similarityService = 
            new com.aiagent.orchestrator.embedding.CosineSimilarityService();
        
        // Create stub EmbeddingService using reflection to avoid constructor issues
        com.aiagent.orchestrator.embedding.EmbeddingService embeddingService = 
            new com.aiagent.orchestrator.embedding.EmbeddingService(
                "http://localhost:11434", "nomic-embed-text", new com.fasterxml.jackson.databind.ObjectMapper()) {
                @Override
                public com.aiagent.orchestrator.embedding.EmbeddingResponse generateEmbedding(
                    com.aiagent.orchestrator.embedding.EmbeddingRequest request) {
                    // Return empty embedding for tests
                    return new com.aiagent.orchestrator.embedding.EmbeddingResponse(
                        request.getSourceId(), java.util.List.of());
                }
            };
        
        com.aiagent.orchestrator.memory.SemanticMemoryQueryService memoryQueryService = 
            new com.aiagent.orchestrator.memory.SemanticMemoryQueryService(
                embeddingService, memoryStore, similarityService);
        adaptivePlanningService = new com.aiagent.orchestrator.adaptive.AdaptivePlanningService(
            memoryQueryService, learningSignalStore);
        riskAnalyzer = new com.aiagent.orchestrator.adaptive.ExecutionRiskAnalyzer(
            memoryQueryService, learningSignalStore);
        strategyEngine = new com.aiagent.orchestrator.adaptive.ToolStrategyEngine(learningSignalStore);
        
        planningService = new PlanningService(new TestAiEnhancedPlanningService());
        contextLoaderService = new ContextLoaderService();
        fileReaderService = new FileReaderService();
        patchProposalService = new PatchProposalService();
        backupService = new BackupService();
        patchSafetyService = new PatchSafetyService();
        patchExecutionService = new PatchExecutionService();
        toolRouterService = new ToolRouterService();
        toolExecutionService = new ToolExecutionService();
        minimalOrchestratorService = new MinimalOrchestratorService();
        
        // Inject dependencies using reflection
        try {
            java.lang.reflect.Field sessionField = ExecutionSessionService.class.getDeclaredField("stateTransitionService");
            sessionField.setAccessible(true);
            sessionField.set(executionSessionService, stateTransitionService);
            
            java.lang.reflect.Field orchestratorSessionField = MinimalOrchestratorService.class.getDeclaredField("executionSessionService");
            orchestratorSessionField.setAccessible(true);
            orchestratorSessionField.set(minimalOrchestratorService, executionSessionService);
            
            java.lang.reflect.Field orchestratorPlanningField = MinimalOrchestratorService.class.getDeclaredField("planningService");
            orchestratorPlanningField.setAccessible(true);
            orchestratorPlanningField.set(minimalOrchestratorService, planningService);
            
            java.lang.reflect.Field orchestratorRouterField = MinimalOrchestratorService.class.getDeclaredField("toolRouterService");
            orchestratorRouterField.setAccessible(true);
            orchestratorRouterField.set(minimalOrchestratorService, toolRouterService);
            
            java.lang.reflect.Field orchestratorToolField = MinimalOrchestratorService.class.getDeclaredField("toolExecutionService");
            orchestratorToolField.setAccessible(true);
            orchestratorToolField.set(minimalOrchestratorService, toolExecutionService);
            
            // Inject services into ToolExecutionService
            java.lang.reflect.Field contextField = ToolExecutionService.class.getDeclaredField("contextLoaderService");
            contextField.setAccessible(true);
            contextField.set(toolExecutionService, contextLoaderService);
            
            java.lang.reflect.Field fileField = ToolExecutionService.class.getDeclaredField("fileReaderService");
            fileField.setAccessible(true);
            fileField.set(toolExecutionService, fileReaderService);
            
            java.lang.reflect.Field patchField = ToolExecutionService.class.getDeclaredField("patchProposalService");
            patchField.setAccessible(true);
            patchField.set(toolExecutionService, patchProposalService);
            
            java.lang.reflect.Field executionField = ToolExecutionService.class.getDeclaredField("patchExecutionService");
            executionField.setAccessible(true);
            executionField.set(toolExecutionService, patchExecutionService);
            
            // Inject ExecutionStatsService into ToolExecutionService
            java.lang.reflect.Field statsField = ToolExecutionService.class.getDeclaredField("executionStatsService");
            statsField.setAccessible(true);
            statsField.set(toolExecutionService, executionStatsService);
            
            // Inject ExecutionStatsService and ToolBiasService into ToolRouterService
            java.lang.reflect.Field routerStatsField = ToolRouterService.class.getDeclaredField("executionStatsService");
            routerStatsField.setAccessible(true);
            routerStatsField.set(toolRouterService, executionStatsService);
            
            java.lang.reflect.Field routerBiasField = ToolRouterService.class.getDeclaredField("toolBiasService");
            routerBiasField.setAccessible(true);
            routerBiasField.set(toolRouterService, toolBiasService);
            
            // Inject adaptive services into MinimalOrchestratorService
            java.lang.reflect.Field adaptiveField = MinimalOrchestratorService.class.getDeclaredField("adaptivePlanningService");
            adaptiveField.setAccessible(true);
            adaptiveField.set(minimalOrchestratorService, adaptivePlanningService);
            
            java.lang.reflect.Field riskField = MinimalOrchestratorService.class.getDeclaredField("riskAnalyzer");
            riskField.setAccessible(true);
            riskField.set(minimalOrchestratorService, riskAnalyzer);
            
            java.lang.reflect.Field strategyField = MinimalOrchestratorService.class.getDeclaredField("strategyEngine");
            strategyField.setAccessible(true);
            strategyField.set(minimalOrchestratorService, strategyEngine);
            
            // Inject safety services into PatchExecutionService
            java.lang.reflect.Field backupField = PatchExecutionService.class.getDeclaredField("backupService");
            backupField.setAccessible(true);
            backupField.set(patchExecutionService, backupService);
            
            java.lang.reflect.Field safetyField = PatchExecutionService.class.getDeclaredField("patchSafetyService");
            safetyField.setAccessible(true);
            safetyField.set(patchExecutionService, patchSafetyService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject dependencies: " + e.getMessage(), e);
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
            ExecutionState.PATCH_APPLYING,
            ExecutionState.COMPLETED
        );
        
        assertEquals(expectedStates, response.getCompletedStates());
    }

    @Test
    void testPatchExecutesSuccessfully_SuccessTrue() {
        OrchestratorTaskRequest request = new OrchestratorTaskRequest("Add caching to UserService");
        
        OrchestratorTaskResponse response = minimalOrchestratorService.execute(request);
        
        assertNotNull(response.getPatchResults());
        assertFalse(response.getPatchResults().isEmpty());
        
        // Verify at least one patch executed successfully
        boolean hasSuccessfulPatch = response.getPatchResults().stream()
            .anyMatch(PatchExecutionResult::isSuccess);
        assertTrue(hasSuccessfulPatch, "Should have at least one successful patch execution");
    }

    @Test
    void testMultiplePatchesProcessed_AllReturned() {
        OrchestratorTaskRequest request = new OrchestratorTaskRequest("Add caching to UserService");
        
        OrchestratorTaskResponse response = minimalOrchestratorService.execute(request);
        
        assertNotNull(response.getPatches());
        assertNotNull(response.getPatchResults());
        
        // Number of patch results should match number of patches
        assertEquals(response.getPatches().size(), response.getPatchResults().size());
    }

    @Test
    void testPartialFailureSafe_OneFailureDoesNotStopExecution() {
        // This test verifies that patch execution continues even if some patches fail
        OrchestratorTaskRequest request = new OrchestratorTaskRequest("Add caching to UserService");
        
        OrchestratorTaskResponse response = minimalOrchestratorService.execute(request);
        
        // Execution should complete successfully even if some patches fail
        assertTrue(response.getCompletedStates().contains(ExecutionState.COMPLETED));
        assertNotNull(response.getPatchResults());
    }

    @Test
    void testEmptyPatchesSafe_ReturnsEmptyList() {
        OrchestratorTaskRequest request = new OrchestratorTaskRequest("");
        
        OrchestratorTaskResponse response = minimalOrchestratorService.execute(request);
        
        assertNotNull(response.getPatchResults());
        // Empty patches should result in empty patch results
        assertTrue(response.getPatchResults().isEmpty());
        // But execution should still complete
        assertTrue(response.getCompletedStates().contains(ExecutionState.COMPLETED));
    }

    @Test
    void testStateFlowIncludesPatchApplying_StateTransitionWorks() {
        OrchestratorTaskRequest request = new OrchestratorTaskRequest("Add caching to UserService");
        
        OrchestratorTaskResponse response = minimalOrchestratorService.execute(request);
        
        // Verify PATCH_APPLYING state is in the flow
        assertTrue(response.getCompletedStates().contains(ExecutionState.PATCH_APPLYING));
        
        // Verify state transitions are working correctly
        List<ExecutionState> states = response.getCompletedStates();
        int verifyingIndex = states.indexOf(ExecutionState.VERIFYING);
        int patchApplyingIndex = states.indexOf(ExecutionState.PATCH_APPLYING);
        int completedIndex = states.indexOf(ExecutionState.COMPLETED);
        
        assertTrue(verifyingIndex < patchApplyingIndex, "VERIFYING should come before PATCH_APPLYING");
        assertTrue(patchApplyingIndex < completedIndex, "PATCH_APPLYING should come before COMPLETED");
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
}