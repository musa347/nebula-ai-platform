package com.aiagent.orchestrator.autonomous;

import com.aiagent.common.dto.OrchestratorTaskRequest;
import com.aiagent.common.dto.OrchestratorTaskResponse;
import com.aiagent.common.enums.ExecutionState;
import com.aiagent.common.model.ExecutionPlan;
import com.aiagent.orchestrator.adaptive.ExecutionRisk;
import com.aiagent.orchestrator.adaptive.ExecutionRiskAnalyzer;
import com.aiagent.orchestrator.service.PlanningService;
import com.aiagent.orchestrator.service.ToolExecutionService;
import com.aiagent.orchestrator.streaming.ExecutionEvent;
import com.aiagent.orchestrator.streaming.ExecutionEventBus;
import com.aiagent.orchestrator.streaming.ExecutionEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AutonomousOrchestrator {
    private static final Logger log = LoggerFactory.getLogger(AutonomousOrchestrator.class);
    private static final int MAX_ATTEMPTS = 10;
    
    @Autowired
    private NextActionEngine nextActionEngine;
    
    @Autowired
    private UnifiedToolExecutor toolExecutor;
    
    @Autowired
    private LearningUpdater learningUpdater;
    
    @Autowired
    private PlanningService planningService;
    
    @Autowired
    private ExecutionRiskAnalyzer riskAnalyzer;
    
    @Autowired(required = false)
    private ExecutionEventBus eventBus;
    
    public OrchestratorTaskResponse execute(OrchestratorTaskRequest request) {
        String executionId = UUID.randomUUID().toString();
        AutonomousExecutionContext context = new AutonomousExecutionContext(executionId, request.getTask());
        
        List<ExecutionState> completedStates = new ArrayList<>();
        completedStates.add(ExecutionState.CREATED);
        
        int attempts = 0;
        
        // AUTONOMOUS LOOP: Continuous decision-making
        while (attempts < MAX_ATTEMPTS) {
            attempts++;
            long startTime = System.currentTimeMillis();
            
            // 1. DECIDE next action
            NextAction action = nextActionEngine.decide(context);
            log.info("Loop iteration {}: action={}, reason={}", attempts, action.getType(), action.getReason());
            
            // Emit TOOL_SELECTED event
            publishEvent(executionId, ExecutionEventType.TOOL_SELECTED, 
                "Action selected: " + action.getType() + " - " + action.getReason());
            
            // 2. EXECUTE action
            boolean success = executeAction(action, context, completedStates);
            
            // Emit TOOL_EXECUTED event
            publishEvent(executionId, ExecutionEventType.TOOL_EXECUTED, 
                "Action executed: " + action.getType() + " - " + (success ? "SUCCESS" : "FAILED"));
            
            // 3. UPDATE context
            long duration = System.currentTimeMillis() - startTime;
            
            // 4. LEARN from execution
            learningUpdater.update(context, success, duration);
            
            // Emit LEARNING_UPDATED event
            publishEvent(executionId, ExecutionEventType.LEARNING_UPDATED, 
                "Learning updated from execution");
            
            // 5. Check completion
            if (action.getType() == ActionType.COMPLETE) {
                log.info("Autonomous execution completed after {} iterations", attempts);
                publishEvent(executionId, ExecutionEventType.COMPLETE, 
                    "Execution completed successfully after " + attempts + " iterations");
                break;
            }
            
            if (!success && action.getType() == ActionType.ANALYZE_FAILURE) {
                log.warn("Execution failed after failure analysis");
                context.setCurrentState(ExecutionState.FAILED);
                completedStates.add(ExecutionState.FAILED);
                publishEvent(executionId, ExecutionEventType.ERROR, 
                    "Execution failed after failure analysis");
                break;
            }
        }
        
        if (attempts >= MAX_ATTEMPTS) {
            log.warn("Max attempts reached, terminating execution");
            context.setCurrentState(ExecutionState.FAILED);
            completedStates.add(ExecutionState.FAILED);
            publishEvent(executionId, ExecutionEventType.ERROR, 
                "Max attempts reached, execution terminated");
        }
        
        return new OrchestratorTaskResponse(
            executionId, 
            completedStates, 
            new ArrayList<>(), 
            new ArrayList<>(), 
            new ArrayList<>(), 
            new ArrayList<>()
        );
    }
    
    private boolean executeAction(NextAction action, AutonomousExecutionContext context, List<ExecutionState> completedStates) {
        try {
            switch (action.getType()) {
                case PLAN:
                    return executePlan(context, completedStates);
                    
                case EXECUTE_TOOL:
                    return executeToolAction(context, completedStates);
                    
                case GENERATE_PATCH:
                    return executeGeneratePatch(context, completedStates);
                    
                case APPLY_PATCH:
                    return executeApplyPatch(context, completedStates);
                    
                case RETRY:
                    return executeRetry(context, completedStates);
                    
                case ANALYZE_FAILURE:
                    return executeAnalyzeFailure(context, completedStates);
                    
                case COMPLETE:
                    context.setCurrentState(ExecutionState.COMPLETED);
                    completedStates.add(ExecutionState.COMPLETED);
                    return true;
                    
                default:
                    log.warn("Unknown action type: {}", action.getType());
                    return false;
            }
        } catch (Exception e) {
            log.error("Action execution failed: {}", e.getMessage(), e);
            return false;
        }
    }
    
    private boolean executePlan(AutonomousExecutionContext context, List<ExecutionState> completedStates) {
        ExecutionPlan plan = planningService.createPlan(context.getTask());
        context.setPlan(plan);
        context.setCurrentState(ExecutionState.PLANNING);
        completedStates.add(ExecutionState.PLANNING);
        
        // Emit PLAN_CREATED event
        publishEvent(context.getExecutionId(), ExecutionEventType.PLAN_CREATED, 
            "Plan created with " + plan.getSteps().size() + " steps");
        
        // Analyze risk
        ExecutionRisk risk = riskAnalyzer.analyzeRisk(context.getTask(), "PLANNING");
        context.setRisk(risk);
        
        // Emit RISK_ANALYZED event
        publishEvent(context.getExecutionId(), ExecutionEventType.RISK_ANALYZED, 
            "Risk score: " + risk.getRiskScore() + " - " + risk.getReason());
        
        log.info("Plan created with {} steps, risk={}", plan.getSteps().size(), risk.getRiskScore());
        return true;
    }
    
    private boolean executeToolAction(AutonomousExecutionContext context, List<ExecutionState> completedStates) {
        // Advance state based on current state
        ExecutionState nextState = getNextState(context.getCurrentState());
        context.setCurrentState(nextState);
        completedStates.add(nextState);
        
        ToolExecutionService.ToolExecutionResult result = toolExecutor.execute(context);
        return result.isSuccess();
    }
    
    private boolean executeGeneratePatch(AutonomousExecutionContext context, List<ExecutionState> completedStates) {
        context.setCurrentState(ExecutionState.VERIFYING);
        if (!completedStates.contains(ExecutionState.VERIFYING)) {
            completedStates.add(ExecutionState.VERIFYING);
        }
        
        ToolExecutionService.ToolExecutionResult result = toolExecutor.execute(context);
        
        // Emit PATCH_GENERATED event
        if (result.isSuccess()) {
            publishEvent(context.getExecutionId(), ExecutionEventType.PATCH_GENERATED, 
                "Patches generated successfully");
            context.setCurrentState(ExecutionState.PATCH_APPLYING);
        }
        
        return result.isSuccess();
    }
    
    private boolean executeApplyPatch(AutonomousExecutionContext context, List<ExecutionState> completedStates) {
        context.setCurrentState(ExecutionState.PATCH_APPLYING);
        if (!completedStates.contains(ExecutionState.PATCH_APPLYING)) {
            completedStates.add(ExecutionState.PATCH_APPLYING);
        }
        
        ToolExecutionService.ToolExecutionResult result = toolExecutor.execute(context);
        
        // Emit PATCH_APPLIED event
        if (result.isSuccess()) {
            publishEvent(context.getExecutionId(), ExecutionEventType.PATCH_APPLIED, 
                "Patches applied successfully");
            context.setCurrentState(ExecutionState.COMPLETED);
        }
        
        return result.isSuccess();
    }
    
    private void publishEvent(String executionId, ExecutionEventType type, String message) {
        if (eventBus != null) {
            ExecutionEvent event = new ExecutionEvent(executionId, type, message);
            eventBus.publish(event);
        }
    }
    
    private boolean executeRetry(AutonomousExecutionContext context, List<ExecutionState> completedStates) {
        log.info("Retrying execution from state: {}", context.getCurrentState());
        // Reset to planning state for retry
        context.setCurrentState(ExecutionState.PLANNING);
        return true;
    }
    
    private boolean executeAnalyzeFailure(AutonomousExecutionContext context, List<ExecutionState> completedStates) {
        log.warn("Analyzing failure for task: {}", context.getTask());
        // Failure analysis complete - mark as failed
        return false;
    }
    
    private ExecutionState getNextState(ExecutionState currentState) {
        switch (currentState) {
            case CREATED:
            case PLANNING:
                return ExecutionState.CONTEXT_LOADING;
            case CONTEXT_LOADING:
                return ExecutionState.EXECUTING;
            case EXECUTING:
                return ExecutionState.VERIFYING;
            default:
                return currentState;
        }
    }
}
