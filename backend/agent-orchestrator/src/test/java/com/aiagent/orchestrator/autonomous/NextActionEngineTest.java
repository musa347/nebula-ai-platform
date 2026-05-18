package com.aiagent.orchestrator.autonomous;

import com.aiagent.common.enums.ExecutionState;
import com.aiagent.common.model.ExecutionPlan;
import com.aiagent.orchestrator.adaptive.ExecutionRisk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NextActionEngineTest {
    
    private NextActionEngine engine;
    
    @BeforeEach
    void setUp() {
        engine = new NextActionEngine();
    }
    
    @Test
    void testEmptyContextReturnsPlan() {
        AutonomousExecutionContext context = new AutonomousExecutionContext("exec-1", "Test task");
        
        NextAction action = engine.decide(context);
        
        assertEquals(ActionType.PLAN, action.getType());
        assertTrue(action.getReason().contains("No execution plan"));
    }
    
    @Test
    void testFailureContextReturnsAnalyzeFailure() {
        AutonomousExecutionContext context = new AutonomousExecutionContext("exec-1", "Test task");
        context.setPlan(new ExecutionPlan("Test task", List.of()));
        context.setCurrentState(ExecutionState.FAILED);
        
        // Add multiple signals to trigger ANALYZE_FAILURE instead of RETRY
        context.addSignal(new com.aiagent.orchestrator.learning.LearningSignal(
            "exec1", false, "tool1", "FAILED", 0.2, "FAILED"));
        context.addSignal(new com.aiagent.orchestrator.learning.LearningSignal(
            "exec2", false, "tool2", "FAILED", 0.2, "FAILED"));
        context.addSignal(new com.aiagent.orchestrator.learning.LearningSignal(
            "exec3", false, "tool3", "FAILED", 0.2, "FAILED"));
        
        NextAction action = engine.decide(context);
        
        assertEquals(ActionType.ANALYZE_FAILURE, action.getType());
        assertTrue(action.getReason().contains("Multiple failures"));
    }
    
    @Test
    void testPatchExistsReturnsApplyPatch() {
        AutonomousExecutionContext context = new AutonomousExecutionContext("exec-1", "Test task");
        context.setPlan(new ExecutionPlan("Test task", List.of()));
        context.setCurrentState(ExecutionState.PATCH_APPLYING);
        
        NextAction action = engine.decide(context);
        
        assertEquals(ActionType.APPLY_PATCH, action.getType());
        assertTrue(action.getReason().contains("Patches ready"));
    }
    
    @Test
    void testHighRiskReturnsAnalyzeFailure() {
        AutonomousExecutionContext context = new AutonomousExecutionContext("exec-1", "Test task");
        context.setPlan(new ExecutionPlan("Test task", List.of()));
        context.setRisk(new ExecutionRisk(0.8, "High risk detected"));
        
        NextAction action = engine.decide(context);
        
        assertEquals(ActionType.ANALYZE_FAILURE, action.getType());
        assertTrue(action.getReason().contains("High risk"));
    }
    
    @Test
    void testVerifyingStateReturnsGeneratePatch() {
        AutonomousExecutionContext context = new AutonomousExecutionContext("exec-1", "Test task");
        context.setPlan(new ExecutionPlan("Test task", List.of()));
        context.setCurrentState(ExecutionState.VERIFYING);
        
        NextAction action = engine.decide(context);
        
        assertEquals(ActionType.GENERATE_PATCH, action.getType());
    }
    
    @Test
    void testCompletedStateReturnsComplete() {
        AutonomousExecutionContext context = new AutonomousExecutionContext("exec-1", "Test task");
        context.setPlan(new ExecutionPlan("Test task", List.of()));
        context.setCurrentState(ExecutionState.COMPLETED);
        
        NextAction action = engine.decide(context);
        
        assertEquals(ActionType.COMPLETE, action.getType());
    }
}
