package com.aiagent.orchestrator.service;

import com.aiagent.common.enums.ExecutionState;
import com.aiagent.common.enums.ToolType;
import com.aiagent.common.model.ToolDecision;
import com.aiagent.orchestrator.learning.ToolBiasService;
import com.aiagent.orchestrator.learning.LearningSignalStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ToolRouterServiceTest {

    private ToolRouterService toolRouterService;
    private ExecutionStatsService executionStatsService;
    private ToolBiasService toolBiasService;

    @BeforeEach
    void setUp() {
        executionStatsService = new ExecutionStatsService();
        LearningSignalStore learningSignalStore = new LearningSignalStore();
        toolBiasService = new ToolBiasService(learningSignalStore);
        toolRouterService = new ToolRouterService();
        // Use reflection to inject the dependencies
        try {
            var statsField = ToolRouterService.class.getDeclaredField("executionStatsService");
            statsField.setAccessible(true);
            statsField.set(toolRouterService, executionStatsService);
            
            var biasField = ToolRouterService.class.getDeclaredField("toolBiasService");
            biasField.setAccessible(true);
            biasField.set(toolRouterService, toolBiasService);
        } catch (Exception e) {
            fail("Failed to inject dependencies: " + e.getMessage());
        }
    }

    @Test
    void testPlanningStateRoutesToRepoSearch() {
        // Rule 1 — Initial Stage: PLANNING → REPO_SEARCH
        ToolDecision decision = toolRouterService.decide(ExecutionState.PLANNING, "Add caching", null);
        
        assertEquals(ToolType.REPO_SEARCH, decision.getToolType());
        assertTrue(decision.getReason().contains("state"));
    }

    @Test
    void testContextLoadingRoutesToSymbolSearch() {
        // Rule 2 — Context Stage: CONTEXT_LOADING → SYMBOL_SEARCH (default)
        ToolDecision decision = toolRouterService.decide(ExecutionState.CONTEXT_LOADING, "Add logging", null);
        
        assertEquals(ToolType.SYMBOL_SEARCH, decision.getToolType());
        assertTrue(decision.getReason().contains("state"));
    }

    @Test
    void testContextLoadingRoutesToDependencyAnalysis() {
        // Rule 2 — Context Stage: CONTEXT_LOADING → DEPENDENCY_ANALYSIS (keyword match)
        ToolDecision decision = toolRouterService.decide(ExecutionState.CONTEXT_LOADING, "Add dependency injection", null);
        
        assertEquals(ToolType.DEPENDENCY_ANALYSIS, decision.getToolType());
        assertTrue(decision.getReason().contains("Best performing tool"));
    }

    @Test
    void testExecutingStateRoutesToFileRead() {
        // Rule 3 — Execution Stage: EXECUTING → FILE_READ
        ToolDecision decision = toolRouterService.decide(ExecutionState.EXECUTING, "Analyze code", null);
        
        assertEquals(ToolType.FILE_READ, decision.getToolType());
        assertTrue(decision.getReason().contains("state"));
    }

    @Test
    void testVerifyingStateRoutesToPatchGenerate() {
        // Rule 4 — Verification Stage: VERIFYING → PATCH_GENERATE
        ToolDecision decision = toolRouterService.decide(ExecutionState.VERIFYING, "Fix bug", null);
        
        assertEquals(ToolType.PATCH_GENERATE, decision.getToolType());
        assertTrue(decision.getReason().contains("state"));
    }

    @Test
    void testPatchApplyingStateRoutesToPatchApply() {
        // Rule 5 — Apply Stage: PATCH_APPLYING → PATCH_APPLY
        ToolDecision decision = toolRouterService.decide(ExecutionState.PATCH_APPLYING, "Apply fixes", null);
        
        assertEquals(ToolType.PATCH_APPLY, decision.getToolType());
        assertTrue(decision.getReason().contains("state"));
    }

    @Test
    void testUnknownStateRoutesToNone() {
        // Rule 6 — Fallback: UNKNOWN STATE → NONE
        ToolDecision decision = toolRouterService.decide(ExecutionState.COMPLETED, "Task done", null);
        
        assertEquals(ToolType.NONE, decision.getToolType());
        assertTrue(decision.getReason().contains("No tools available"));
    }

    @Test
    void testNullStateRoutesToNone() {
        // Safe fallback for null state
        ToolDecision decision = toolRouterService.decide(null, "Some task", null);
        
        assertEquals(ToolType.NONE, decision.getToolType());
        assertTrue(decision.getReason().contains("No state provided"));
    }
    
    @Test
    void testAdaptiveRoutingPrefersSuccessfulTool() {
        // Record stats to show SYMBOL_SEARCH is more successful than DEPENDENCY_ANALYSIS
        executionStatsService.recordSuccess(ToolType.SYMBOL_SEARCH, 100);
        executionStatsService.recordSuccess(ToolType.SYMBOL_SEARCH, 100);
        executionStatsService.recordSuccess(ToolType.SYMBOL_SEARCH, 100);
        executionStatsService.recordSuccess(ToolType.SYMBOL_SEARCH, 100);
        executionStatsService.recordFailure(ToolType.SYMBOL_SEARCH, 100);
        
        executionStatsService.recordSuccess(ToolType.DEPENDENCY_ANALYSIS, 150);
        executionStatsService.recordFailure(ToolType.DEPENDENCY_ANALYSIS, 150);
        executionStatsService.recordFailure(ToolType.DEPENDENCY_ANALYSIS, 150);
        executionStatsService.recordFailure(ToolType.DEPENDENCY_ANALYSIS, 150);
        executionStatsService.recordFailure(ToolType.DEPENDENCY_ANALYSIS, 150);
        
        ToolDecision decision = toolRouterService.decide(ExecutionState.CONTEXT_LOADING, "analyze code", null);
        
        assertEquals(ToolType.SYMBOL_SEARCH, decision.getToolType());
        assertTrue(decision.getReason().contains("Best performing tool"));
    }
    
    @Test
    void testAdaptiveRoutingPenalizesSlowTool() {
        // Record stats to show DEPENDENCY_ANALYSIS is slower than SYMBOL_SEARCH
        executionStatsService.recordSuccess(ToolType.SYMBOL_SEARCH, 200);
        executionStatsService.recordFailure(ToolType.SYMBOL_SEARCH, 200);
        
        executionStatsService.recordSuccess(ToolType.DEPENDENCY_ANALYSIS, 2000);
        executionStatsService.recordFailure(ToolType.DEPENDENCY_ANALYSIS, 2000);
        
        ToolDecision decision = toolRouterService.decide(ExecutionState.CONTEXT_LOADING, "analyze code", null);
        
        assertEquals(ToolType.SYMBOL_SEARCH, decision.getToolType());
        assertTrue(decision.getReason().contains("Best performing tool"));
    }
    
    @Test
    void testAdaptiveRoutingWithTaskBonus() {
        // Record equal stats but task should give bonus to DEPENDENCY_ANALYSIS
        executionStatsService.recordSuccess(ToolType.SYMBOL_SEARCH, 100);
        executionStatsService.recordFailure(ToolType.SYMBOL_SEARCH, 100);
        
        executionStatsService.recordSuccess(ToolType.DEPENDENCY_ANALYSIS, 100);
        executionStatsService.recordFailure(ToolType.DEPENDENCY_ANALYSIS, 100);
        
        ToolDecision decision = toolRouterService.decide(ExecutionState.CONTEXT_LOADING, "analyze dependency imports", null);
        
        assertEquals(ToolType.DEPENDENCY_ANALYSIS, decision.getToolType());
        assertTrue(decision.getReason().contains("Best performing tool"));
    }
    
    @Test
    void testEmptyStatsStillWorks() {
        // No recorded stats (new tools with no history)
        ToolDecision decision = toolRouterService.decide(ExecutionState.CONTEXT_LOADING, "analyze code", null);
        
        // Should still make a decision (first tool in candidates list)
        assertEquals(ToolType.SYMBOL_SEARCH, decision.getToolType());
        assertTrue(decision.getReason().contains("Best performing tool"));
    }



}