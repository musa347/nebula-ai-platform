package com.aiagent.orchestrator.autonomous;

import com.aiagent.common.enums.ToolType;
import com.aiagent.common.model.ToolDecision;
import com.aiagent.orchestrator.service.ToolExecutionService;
import com.aiagent.orchestrator.service.ToolRouterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UnifiedToolExecutor {
    private static final Logger log = LoggerFactory.getLogger(UnifiedToolExecutor.class);
    
    @Autowired
    private ToolRouterService toolRouterService;
    
    @Autowired
    private ToolExecutionService toolExecutionService;
    
    public ToolExecutionService.ToolExecutionResult execute(AutonomousExecutionContext context) {
        // Route to correct tool using ORCH-013 bias and ORCH-014 strategy
        ToolDecision decision = toolRouterService.decide(
            context.getCurrentState(), 
            context.getTask(), 
            null
        );
        
        log.info("Unified executor: state={}, tool={}, reason={}", 
            context.getCurrentState(), decision.getToolType(), decision.getReason());
        
        // Execute tool
        ToolExecutionService.ToolExecutionResult result = toolExecutionService.execute(
            decision, 
            context.getTask(), 
            null
        );
        
        // Store decision in context
        context.setLastToolDecision(decision);
        
        return result;
    }
}
