package com.aiagent.orchestrator.context;

import com.aiagent.common.model.DependencyEdge;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MultiFileContextServiceTest {
    
    private final MultiFileContextService service = new MultiFileContextService();
    
    @Test
    void multipleFilesFormatted() {
        List<RelatedContext> related = List.of(
            new RelatedContext("UserRepository", "FIELD"),
            new RelatedContext("CacheService", "INJECTION")
        );
        
        List<DependencyEdge> deps = List.of(
            new DependencyEdge("UserService", "UserRepository", "FIELD")
        );
        
        String context = service.assembleContext("UserService", related, deps);
        
        assertTrue(context.contains("PRIMARY FILE:"));
        assertTrue(context.contains("UserService"));
        assertTrue(context.contains("RELATED FILES:"));
        assertTrue(context.contains("UserRepository"));
        assertTrue(context.contains("DEPENDENCY SUMMARY:"));
    }
    
    @Test
    void dependencySummariesIncluded() {
        List<DependencyEdge> deps = List.of(
            new DependencyEdge("UserService", "UserRepository", "FIELD"),
            new DependencyEdge("UserService", "CacheService", "INJECTION")
        );
        
        String context = service.assembleContext("UserService", List.of(), deps);
        
        assertTrue(context.contains("UserService -> UserRepository"));
        assertTrue(context.contains("UserService -> CacheService"));
    }
    
    @Test
    void emptyRelationsSafe() {
        String context = service.assembleContext("UserService", List.of(), List.of());
        
        assertTrue(context.contains("PRIMARY FILE:"));
        assertTrue(context.contains("UserService"));
        assertFalse(context.contains("RELATED FILES:"));
    }
}
