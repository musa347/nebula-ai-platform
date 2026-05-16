package com.aiagent.orchestrator.service;

import com.aiagent.common.enums.ToolType;
import com.aiagent.common.model.ExecutionPlan;
import com.aiagent.common.model.PlanStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlanningServiceTest {

    private PlanningService planningService;

    @BeforeEach
    void setUp() {
        planningService = new PlanningService();
    }

    @Test
    void testCachingTaskGeneratesPatchSteps() {
        // Rule test: caching task generates patch steps
        String task = "Add caching to UserService";
        
        ExecutionPlan plan = planningService.createPlan(task);
        
        assertNotNull(plan);
        assertNotNull(plan.getExecutionId());
        assertFalse(plan.getSteps().isEmpty());
        
        // Verify includes PATCH_GENERATE + PATCH_APPLY
        List<ToolType> toolTypes = plan.getSteps().stream()
            .map(PlanStep::getToolType)
            .toList();
        
        assertTrue(toolTypes.contains(ToolType.PATCH_GENERATE), "Should include PATCH_GENERATE");
        assertTrue(toolTypes.contains(ToolType.PATCH_APPLY), "Should include PATCH_APPLY");
        
        // Verify correct order
        int generateIndex = toolTypes.indexOf(ToolType.PATCH_GENERATE);
        int applyIndex = toolTypes.indexOf(ToolType.PATCH_APPLY);
        assertTrue(generateIndex < applyIndex, "PATCH_GENERATE should come before PATCH_APPLY");
    }

    @Test
    void testReadOnlyTaskSkipsPatch() {
        // Rule test: read-only task skips patch
        String task = "Analyze UserService implementation";
        
        ExecutionPlan plan = planningService.createPlan(task);
        
        assertNotNull(plan);
        List<ToolType> toolTypes = plan.getSteps().stream()
            .map(PlanStep::getToolType)
            .toList();
        
        // Verify no PATCH steps
        assertFalse(toolTypes.contains(ToolType.PATCH_GENERATE), "Should not include PATCH_GENERATE");
        assertFalse(toolTypes.contains(ToolType.PATCH_APPLY), "Should not include PATCH_APPLY");
    }

    @Test
    void testSymbolBasedTaskIncludesSymbolSearch() {
        // Rule test: symbol-based task includes SYMBOL_SEARCH
        String task = "Fix bug in UserService";
        
        ExecutionPlan plan = planningService.createPlan(task);
        
        assertNotNull(plan);
        List<ToolType> toolTypes = plan.getSteps().stream()
            .map(PlanStep::getToolType)
            .toList();
        
        // Verify correct detection
        assertTrue(toolTypes.contains(ToolType.SYMBOL_SEARCH), "Should include SYMBOL_SEARCH");
        
        // Verify target extraction
        PlanStep symbolStep = plan.getSteps().stream()
            .filter(step -> step.getToolType() == ToolType.SYMBOL_SEARCH)
            .findFirst()
            .orElse(null);
        
        assertNotNull(symbolStep);
        assertEquals("UserService", symbolStep.getTarget());
    }

    @Test
    void testOrderingIsAlwaysDeterministic() {
        // Rule test: ordering is always deterministic
        String task = "Add logging to UserService";
        
        ExecutionPlan plan1 = planningService.createPlan(task);
        ExecutionPlan plan2 = planningService.createPlan(task);
        
        // Verify step order is always consistent
        List<ToolType> toolTypes1 = plan1.getSteps().stream()
            .map(PlanStep::getToolType)
            .toList();
        List<ToolType> toolTypes2 = plan2.getSteps().stream()
            .map(PlanStep::getToolType)
            .toList();
        
        assertEquals(toolTypes1, toolTypes2, "Tool type order should be deterministic");
        
        // Verify step orders are sequential
        for (int i = 0; i < plan1.getSteps().size(); i++) {
            assertEquals(i + 1, plan1.getSteps().get(i).getOrder(), "Step order should be sequential");
        }
    }

    @Test
    void testEmptyTaskReturnsMinimalSafePlan() {
        // Rule test: empty task returns minimal safe plan
        ExecutionPlan emptyPlan = planningService.createPlan("");
        ExecutionPlan nullPlan = planningService.createPlan(null);
        
        // Verify returns minimal plan
        assertNotNull(emptyPlan);
        assertNotNull(nullPlan);
        
        assertEquals(1, emptyPlan.getSteps().size(), "Empty task should return minimal plan");
        assertEquals(1, nullPlan.getSteps().size(), "Null task should return minimal plan");
        
        assertEquals(ToolType.CONTEXT_LOAD, emptyPlan.getSteps().get(0).getToolType());
        assertEquals(ToolType.CONTEXT_LOAD, nullPlan.getSteps().get(0).getToolType());
    }

    @Test
    void testComplexTaskFullFlow() {
        // Integration test: complex task with all steps
        String task = "Update caching mechanism in UserService";
        
        ExecutionPlan plan = planningService.createPlan(task);
        
        assertNotNull(plan);
        assertEquals(5, plan.getSteps().size(), "Should have all 5 steps");
        
        List<ToolType> expectedOrder = List.of(
            ToolType.SYMBOL_SEARCH,
            ToolType.CONTEXT_LOAD,
            ToolType.FILE_READ,
            ToolType.PATCH_GENERATE,
            ToolType.PATCH_APPLY
        );
        
        List<ToolType> actualOrder = plan.getSteps().stream()
            .map(PlanStep::getToolType)
            .toList();
        
        assertEquals(expectedOrder, actualOrder, "Should follow expected tool order");
    }

    @Test
    void testContextTargetExtraction() {
        // Test context target extraction logic
        String cacheTask = "Add Redis caching to service";
        ExecutionPlan cachePlan = planningService.createPlan(cacheTask);
        
        PlanStep contextStep = cachePlan.getSteps().stream()
            .filter(step -> step.getToolType() == ToolType.CONTEXT_LOAD)
            .findFirst()
            .orElse(null);
        
        assertNotNull(contextStep);
        assertEquals("cache", contextStep.getTarget());
    }

    @Test
    void testFileTargetExtraction() {
        // Test file target extraction logic
        String task = "Fix UserService bug";
        ExecutionPlan plan = planningService.createPlan(task);
        
        PlanStep fileStep = plan.getSteps().stream()
            .filter(step -> step.getToolType() == ToolType.FILE_READ)
            .findFirst()
            .orElse(null);
        
        assertNotNull(fileStep);
        assertEquals("UserService.java", fileStep.getTarget());
    }

    @Test
    void testMutationVsReadOnlyDetection() {
        // Test mutation vs read-only task detection
        String[] mutationTasks = {
            "Add feature to UserService",
            "Fix bug in UserService", 
            "Update UserService implementation",
            "Create new UserService method"
        };
        
        String[] readOnlyTasks = {
            "Analyze UserService code",
            "Review UserService implementation",
            "Check UserService performance",
            "Inspect UserService methods"
        };
        
        for (String task : mutationTasks) {
            ExecutionPlan plan = planningService.createPlan(task);
            List<ToolType> toolTypes = plan.getSteps().stream()
                .map(PlanStep::getToolType)
                .toList();
            assertTrue(toolTypes.contains(ToolType.PATCH_GENERATE), 
                "Mutation task should include PATCH_GENERATE: " + task);
        }
        
        for (String task : readOnlyTasks) {
            ExecutionPlan plan = planningService.createPlan(task);
            List<ToolType> toolTypes = plan.getSteps().stream()
                .map(PlanStep::getToolType)
                .toList();
            assertFalse(toolTypes.contains(ToolType.PATCH_GENERATE), 
                "Read-only task should not include PATCH_GENERATE: " + task);
        }
    }
}