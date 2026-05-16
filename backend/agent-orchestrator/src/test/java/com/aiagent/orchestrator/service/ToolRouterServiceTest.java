package com.aiagent.orchestrator.service;

import com.aiagent.common.enums.ExecutionState;
import com.aiagent.common.enums.ToolType;
import com.aiagent.common.model.ToolDecision;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ToolRouterServiceTest {

    private ToolRouterService toolRouterService;

    @BeforeEach
    void setUp() {
        toolRouterService = new ToolRouterService();
    }

    @Test
    void testPlanningStateRoutesToRepoSearch() {
        // Rule 1 — Initial Stage: PLANNING → REPO_SEARCH
        ToolDecision decision = toolRouterService.decide(ExecutionState.PLANNING, "Add caching", null);
        
        assertEquals(ToolType.REPO_SEARCH, decision.getToolType());
        assertTrue(decision.getReason().contains("Find files"));
    }

    @Test
    void testContextLoadingRoutesToSymbolSearch() {
        // Rule 2 — Context Stage: CONTEXT_LOADING → SYMBOL_SEARCH (default)
        ToolDecision decision = toolRouterService.decide(ExecutionState.CONTEXT_LOADING, "Add logging", null);
        
        assertEquals(ToolType.SYMBOL_SEARCH, decision.getToolType());
        assertTrue(decision.getReason().contains("symbol search"));
    }

    @Test
    void testContextLoadingRoutesToDependencyAnalysis() {
        // Rule 2 — Context Stage: CONTEXT_LOADING → DEPENDENCY_ANALYSIS (keyword match)
        ToolDecision decision = toolRouterService.decide(ExecutionState.CONTEXT_LOADING, "Add dependency injection", null);
        
        assertEquals(ToolType.DEPENDENCY_ANALYSIS, decision.getToolType());
        assertTrue(decision.getReason().contains("dependency"));
    }

    @Test
    void testExecutingStateRoutesToFileRead() {
        // Rule 3 — Execution Stage: EXECUTING → FILE_READ
        ToolDecision decision = toolRouterService.decide(ExecutionState.EXECUTING, "Analyze code", null);
        
        assertEquals(ToolType.FILE_READ, decision.getToolType());
        assertTrue(decision.getReason().contains("Inspect code"));
    }

    @Test
    void testVerifyingStateRoutesToPatchGenerate() {
        // Rule 4 — Verification Stage: VERIFYING → PATCH_GENERATE
        ToolDecision decision = toolRouterService.decide(ExecutionState.VERIFYING, "Fix bug", null);
        
        assertEquals(ToolType.PATCH_GENERATE, decision.getToolType());
        assertTrue(decision.getReason().contains("Propose changes"));
    }

    @Test
    void testPatchApplyingStateRoutesToPatchApply() {
        // Rule 5 — Apply Stage: PATCH_APPLYING → PATCH_APPLY
        ToolDecision decision = toolRouterService.decide(ExecutionState.PATCH_APPLYING, "Apply fixes", null);
        
        assertEquals(ToolType.PATCH_APPLY, decision.getToolType());
        assertTrue(decision.getReason().contains("Apply generated patches"));
    }

    @Test
    void testUnknownStateRoutesToNone() {
        // Rule 6 — Fallback: UNKNOWN STATE → NONE
        ToolDecision decision = toolRouterService.decide(ExecutionState.COMPLETED, "Task done", null);
        
        assertEquals(ToolType.NONE, decision.getToolType());
        assertTrue(decision.getReason().contains("No tool needed"));
    }

    @Test
    void testNullStateRoutesToNone() {
        // Safe fallback for null state
        ToolDecision decision = toolRouterService.decide(null, "Some task", null);
        
        assertEquals(ToolType.NONE, decision.getToolType());
        assertTrue(decision.getReason().contains("No state provided"));
    }

    @Test
    void testContextStageKeywordMatching() {
        // Test symbol-related keywords
        ToolDecision symbolDecision = toolRouterService.decide(ExecutionState.CONTEXT_LOADING, "Find method signatures", null);
        assertEquals(ToolType.SYMBOL_SEARCH, symbolDecision.getToolType());
        
        // Test import-related keywords
        ToolDecision importDecision = toolRouterService.decide(ExecutionState.CONTEXT_LOADING, "Analyze import statements", null);
        assertEquals(ToolType.DEPENDENCY_ANALYSIS, importDecision.getToolType());
        
        // Test library-related keywords
        ToolDecision libraryDecision = toolRouterService.decide(ExecutionState.CONTEXT_LOADING, "Check library usage", null);
        assertEquals(ToolType.DEPENDENCY_ANALYSIS, libraryDecision.getToolType());
    }
}