package com.aiagent.orchestrator.service;

import com.aiagent.common.enums.ToolType;
import com.aiagent.common.model.ExecutionPlan;
import com.aiagent.common.model.PlanStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlanningServiceFeatureFlagTest {

    private PlanningService planningService;
    private TestAiEnhancedPlanningService aiEnhancedPlanningService;

    @BeforeEach
    void setUp() {
        aiEnhancedPlanningService = new TestAiEnhancedPlanningService();
        planningService = new PlanningService(aiEnhancedPlanningService);
    }

    @Test
    void testAiEnabledEnhancedPlanReturned() {
        // Given
        ReflectionTestUtils.setField(planningService, "aiPlanningEnabled", true);
        aiEnhancedPlanningService.setShouldReturnEnhanced(true);
        String task = "Add caching to UserService";

        // When
        ExecutionPlan result = planningService.createPlan(task);

        // Then
        assertNotNull(result);
        assertEquals("enhanced-plan", result.getExecutionId());
        assertTrue(aiEnhancedPlanningService.wasEnhanceCalled());
    }

    @Test
    void testAiEnabledFallbackToDeterministic() {
        // Given
        ReflectionTestUtils.setField(planningService, "aiPlanningEnabled", true);
        aiEnhancedPlanningService.setShouldReturnEnhanced(false);
        String task = "Add caching to UserService";

        // When
        ExecutionPlan result = planningService.createPlan(task);

        // Then
        assertNotNull(result);
        assertNotEquals("enhanced-plan", result.getExecutionId());
        assertEquals(5, result.getSteps().size()); // Deterministic plan
        assertTrue(aiEnhancedPlanningService.wasEnhanceCalled());
    }

    @Test
    void testAiDisabledDeterministicOnly() {
        // Given
        ReflectionTestUtils.setField(planningService, "aiPlanningEnabled", false);
        aiEnhancedPlanningService.setShouldReturnEnhanced(true);
        String task = "Add caching to UserService";

        // When
        ExecutionPlan result = planningService.createPlan(task);

        // Then
        assertNotNull(result);
        assertNotEquals("enhanced-plan", result.getExecutionId());
        assertEquals(5, result.getSteps().size()); // Deterministic plan
        assertFalse(aiEnhancedPlanningService.wasEnhanceCalled());
    }

    @Test
    void testAiFailureFallbackSafe() {
        // Given
        ReflectionTestUtils.setField(planningService, "aiPlanningEnabled", true);
        aiEnhancedPlanningService.setShouldThrowException(true);
        String task = "Add caching to UserService";

        // When
        ExecutionPlan result = planningService.createPlan(task);

        // Then
        assertNotNull(result);
        assertNotEquals("enhanced-plan", result.getExecutionId());
        assertEquals(5, result.getSteps().size()); // Deterministic plan
        assertTrue(aiEnhancedPlanningService.wasEnhanceCalled());
    }


}