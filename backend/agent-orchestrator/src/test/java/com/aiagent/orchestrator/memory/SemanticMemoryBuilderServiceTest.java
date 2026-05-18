package com.aiagent.orchestrator.memory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SemanticMemoryBuilderServiceTest {
    
    private final SemanticMemoryBuilderService service = new SemanticMemoryBuilderService();
    
    @Test
    void successExecutionCreatesCorrectMemoryType() {
        SemanticMemoryEntry entry = service.buildSuccessMemory("exec-1", "test task", "completed");
        
        assertNotNull(entry.getId());
        assertEquals("exec-1", entry.getExecutionId());
        assertEquals("SUCCESS", entry.getOutcome());
        assertEquals("EXECUTION", entry.getSourceType());
    }
    
    @Test
    void failureExecutionCreatesFailureMemory() {
        SemanticMemoryEntry entry = service.buildFailureMemory("exec-1", "test task", "error occurred");
        
        assertEquals("FAILURE", entry.getOutcome());
        assertEquals("FAILURE", entry.getSourceType());
        assertTrue(entry.getSummary().contains("Failure"));
    }
    
    @Test
    void patchExecutionCreatesPatchMemory() {
        SemanticMemoryEntry entry = service.buildPatchMemory("exec-1", "test task", "added method");
        
        assertEquals("SUCCESS", entry.getOutcome());
        assertEquals("PATCH", entry.getSourceType());
        assertTrue(entry.getSummary().contains("Patch applied"));
    }
}
