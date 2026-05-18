package com.aiagent.orchestrator.context;

import com.aiagent.common.model.DependencyEdge;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RelatedContextServiceTest {
    
    private final RelatedContextService service = new RelatedContextService();
    
    @Test
    void directDependencyExpansion() {
        List<DependencyEdge> deps = List.of(
            new DependencyEdge("UserService", "UserRepository", "FIELD"),
            new DependencyEdge("UserService", "CacheService", "INJECTION")
        );
        
        List<RelatedContext> related = service.expandContext("UserService", deps);
        
        assertEquals(2, related.size());
        assertEquals("UserRepository", related.get(0).getFile());
        assertEquals("FIELD", related.get(0).getRelationType());
    }
    
    @Test
    void maxFileLimitEnforced() {
        List<DependencyEdge> deps = List.of(
            new DependencyEdge("Service", "Dep1", "FIELD"),
            new DependencyEdge("Service", "Dep2", "FIELD"),
            new DependencyEdge("Service", "Dep3", "FIELD"),
            new DependencyEdge("Service", "Dep4", "FIELD"),
            new DependencyEdge("Service", "Dep5", "FIELD"),
            new DependencyEdge("Service", "Dep6", "FIELD"),
            new DependencyEdge("Service", "Dep7", "FIELD")
        );
        
        List<RelatedContext> related = service.expandContext("Service", deps);
        
        assertTrue(related.size() <= 5);
    }
    
    @Test
    void noDuplicates() {
        List<DependencyEdge> deps = List.of(
            new DependencyEdge("Service", "Repository", "FIELD"),
            new DependencyEdge("Service", "Repository", "INJECTION")
        );
        
        List<RelatedContext> related = service.expandContext("Service", deps);
        
        assertEquals(1, related.size());
    }
    
    @Test
    void emptyDependencyHandling() {
        List<RelatedContext> related = service.expandContext("Service", List.of());
        
        assertTrue(related.isEmpty());
    }
}
