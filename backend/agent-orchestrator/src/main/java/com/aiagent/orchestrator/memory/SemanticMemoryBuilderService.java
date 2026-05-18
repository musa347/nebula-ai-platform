package com.aiagent.orchestrator.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SemanticMemoryBuilderService {
    private static final Logger log = LoggerFactory.getLogger(SemanticMemoryBuilderService.class);
    
    public SemanticMemoryEntry buildSuccessMemory(String executionId, String task, String summary) {
        String id = UUID.randomUUID().toString();
        
        SemanticMemoryEntry entry = new SemanticMemoryEntry();
        entry.setId(id);
        entry.setExecutionId(executionId);
        entry.setTask(task);
        entry.setOutcome("SUCCESS");
        entry.setSummary(summary != null ? summary : "Successful execution");
        entry.setSourceType("EXECUTION");
        
        log.debug("Built success memory: id={}, executionId={}", id, executionId);
        return entry;
    }
    
    public SemanticMemoryEntry buildFailureMemory(String executionId, String task, String failureReason) {
        String id = UUID.randomUUID().toString();
        
        SemanticMemoryEntry entry = new SemanticMemoryEntry();
        entry.setId(id);
        entry.setExecutionId(executionId);
        entry.setTask(task);
        entry.setOutcome("FAILURE");
        entry.setSummary("Failure: " + (failureReason != null ? failureReason : "Unknown"));
        entry.setSourceType("FAILURE");
        
        log.debug("Built failure memory: id={}, executionId={}", id, executionId);
        return entry;
    }
    
    public SemanticMemoryEntry buildPatchMemory(String executionId, String task, String patchDescription) {
        String id = UUID.randomUUID().toString();
        
        SemanticMemoryEntry entry = new SemanticMemoryEntry();
        entry.setId(id);
        entry.setExecutionId(executionId);
        entry.setTask(task);
        entry.setOutcome("SUCCESS");
        entry.setSummary("Patch applied: " + (patchDescription != null ? patchDescription : "Code change"));
        entry.setSourceType("PATCH");
        
        log.debug("Built patch memory: id={}, executionId={}", id, executionId);
        return entry;
    }
}
