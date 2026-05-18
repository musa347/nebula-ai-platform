package com.aiagent.orchestrator.context;

import com.aiagent.common.model.DependencyEdge;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AiMultiFilePromptServiceTest {
    
    private final AiMultiFilePromptService service = new AiMultiFilePromptService();
    
    @Test
    void allSectionsPresent() {
        List<RelatedContext> related = List.of(new RelatedContext("UserRepository", "FIELD"));
        List<DependencyEdge> deps = List.of(new DependencyEdge("UserService", "UserRepository", "FIELD"));
        
        String prompt = service.buildPrompt("Add caching", "UserService", related, deps);
        
        assertTrue(prompt.contains("TASK:"));
        assertTrue(prompt.contains("PRIMARY FILE:"));
        assertTrue(prompt.contains("RELATED FILES:"));
        assertTrue(prompt.contains("DEPENDENCY RELATIONSHIPS:"));
        assertTrue(prompt.contains("Rules:"));
    }
    
    @Test
    void dependencySectionGenerated() {
        List<DependencyEdge> deps = List.of(
            new DependencyEdge("UserService", "UserRepository", "FIELD"),
            new DependencyEdge("UserService", "CacheService", "INJECTION")
        );
        
        String prompt = service.buildPrompt("Task", "UserService", List.of(), deps);
        
        assertTrue(prompt.contains("UserService -> UserRepository"));
        assertTrue(prompt.contains("UserService -> CacheService"));
    }
    
    @Test
    void nullSafeFormatting() {
        String prompt = service.buildPrompt(null, null, null, null);
        
        assertNotNull(prompt);
        assertTrue(prompt.contains("No task specified"));
        assertTrue(prompt.contains("None"));
    }
}
