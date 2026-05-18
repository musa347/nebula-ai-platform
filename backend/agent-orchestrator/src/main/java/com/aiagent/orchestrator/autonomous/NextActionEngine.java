package com.aiagent.orchestrator.autonomous;

import com.aiagent.common.enums.ExecutionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NextActionEngine {
    private static final Logger log = LoggerFactory.getLogger(NextActionEngine.class);
    
    public NextAction decide(AutonomousExecutionContext context) {
        // Rule 1: No plan → PLAN
        if (context.getPlan() == null) {
            return new NextAction(ActionType.PLAN, "No execution plan exists");
        }
        
        // Rule 2: High risk → ANALYZE_FAILURE
        if (context.getRisk() != null && context.getRisk().getRiskScore() > 0.7) {
            return new NextAction(ActionType.ANALYZE_FAILURE, "High risk detected: " + context.getRisk().getReason());
        }
        
        // Rule 3: Failed state → RETRY or ANALYZE_FAILURE
        if (context.getCurrentState() == ExecutionState.FAILED) {
            if (context.getSignals().size() < 3) {
                return new NextAction(ActionType.RETRY, "Execution failed, retry attempt");
            } else {
                return new NextAction(ActionType.ANALYZE_FAILURE, "Multiple failures detected");
            }
        }
        
        // Rule 4: Patch applying state → APPLY_PATCH
        if (context.getCurrentState() == ExecutionState.PATCH_APPLYING) {
            return new NextAction(ActionType.APPLY_PATCH, "Patches ready to apply");
        }
        
        // Rule 5: Verifying state → GENERATE_PATCH
        if (context.getCurrentState() == ExecutionState.VERIFYING) {
            return new NextAction(ActionType.GENERATE_PATCH, "Verification complete, generate patches");
        }
        
        // Rule 6: Completed state → COMPLETE
        if (context.getCurrentState() == ExecutionState.COMPLETED) {
            return new NextAction(ActionType.COMPLETE, "Execution completed successfully");
        }
        
        // Rule 7: Default → EXECUTE_TOOL
        return new NextAction(ActionType.EXECUTE_TOOL, "Continue execution in state: " + context.getCurrentState());
    }
}
