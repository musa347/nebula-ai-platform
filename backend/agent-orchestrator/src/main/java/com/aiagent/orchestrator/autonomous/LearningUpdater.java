package com.aiagent.orchestrator.autonomous;

import com.aiagent.orchestrator.learning.ExecutionLearningSignalService;
import com.aiagent.orchestrator.learning.LearningSignal;
import com.aiagent.orchestrator.memory.SemanticMemoryBuilderService;
import com.aiagent.orchestrator.memory.SemanticMemoryEntry;
import com.aiagent.orchestrator.memory.SemanticMemoryIndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LearningUpdater {
    private static final Logger log = LoggerFactory.getLogger(LearningUpdater.class);
    
    @Autowired
    private ExecutionLearningSignalService learningSignalService;
    
    @Autowired
    private SemanticMemoryBuilderService memoryBuilderService;
    
    @Autowired
    private SemanticMemoryIndexService memoryIndexService;
    
    public void update(AutonomousExecutionContext context, boolean success, long durationMs) {
        String toolName = context.getLastToolDecision() != null 
            ? context.getLastToolDecision().getToolType().name() 
            : "UNKNOWN";
        
        // Generate learning signal
        double score = success ? 0.8 : 0.2;
        LearningSignal signal = learningSignalService.generateSignal(
            context.getExecutionId(),
            success,
            toolName,
            context.getCurrentState().name(),
            score,
            false
        );
        
        context.addSignal(signal);
        log.debug("Learning signal recorded: tool={}, outcome={}", toolName, signal.getOutcome());
        
        // Store semantic memory
        SemanticMemoryEntry memory;
        if (success) {
            memory = memoryBuilderService.buildSuccessMemory(
                context.getExecutionId(),
                context.getTask(),
                "Tool " + toolName + " executed successfully"
            );
        } else {
            memory = memoryBuilderService.buildFailureMemory(
                context.getExecutionId(),
                context.getTask(),
                "Tool " + toolName + " failed"
            );
        }
        
        if (memory != null) {
            memoryIndexService.indexMemory(memory);
            context.addMemory(memory);
            log.debug("Semantic memory stored: id={}", memory.getId());
        }
    }
}
