package com.aiagent.orchestrator.service;

import com.aiagent.common.enums.ExecutionState;
import com.aiagent.common.enums.ToolType;
import com.aiagent.common.model.ToolDecision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ToolRouterService {

    private static final Logger log = LoggerFactory.getLogger(ToolRouterService.class);

    public ToolDecision decide(ExecutionState currentState, String task, String availableContext) {
        if (currentState == null) {
            return new ToolDecision(ToolType.NONE, "No state provided");
        }

        ToolDecision decision = routeByState(currentState, task, availableContext);
        log.debug("Tool routing decision for state {}: {} - {}", currentState, decision.getToolType(), decision.getReason());
        return decision;
    }

    private ToolDecision routeByState(ExecutionState state, String task, String availableContext) {
        switch (state) {
            case PLANNING:
                // Rule 1 — Initial Stage
                return new ToolDecision(ToolType.REPO_SEARCH, "Find files relevant to task");

            case CONTEXT_LOADING:
                // Rule 2 — Context Stage
                return routeContextStage(task, availableContext);

            case EXECUTING:
                // Rule 3 — Execution Stage
                return new ToolDecision(ToolType.FILE_READ, "Inspect code for implementation details");

            case VERIFYING:
                // Rule 4 — Verification Stage
                return new ToolDecision(ToolType.PATCH_GENERATE, "Propose changes based on analysis");

            case PATCH_APPLYING:
                // Rule 5 — Apply Stage
                return new ToolDecision(ToolType.PATCH_APPLY, "Apply generated patches to files");

            default:
                // Rule 6 — Fallback
                return new ToolDecision(ToolType.NONE, "No tool needed for state: " + state);
        }
    }

    private ToolDecision routeContextStage(String task, String availableContext) {
        if (task == null) {
            return new ToolDecision(ToolType.SYMBOL_SEARCH, "Default symbol search for context loading");
        }

        String lowerTask = task.toLowerCase();
        
        // Choose between SYMBOL_SEARCH and DEPENDENCY_ANALYSIS based on task keywords
        if (lowerTask.contains("dependency") || lowerTask.contains("import") || lowerTask.contains("library")) {
            return new ToolDecision(ToolType.DEPENDENCY_ANALYSIS, "Task involves dependency analysis");
        }
        
        if (lowerTask.contains("symbol") || lowerTask.contains("method") || lowerTask.contains("class")) {
            return new ToolDecision(ToolType.SYMBOL_SEARCH, "Task involves symbol-level analysis");
        }
        
        // Default to symbol search for most context loading scenarios
        return new ToolDecision(ToolType.SYMBOL_SEARCH, "Refine understanding through symbol search");
    }
}