package com.aiagent.orchestrator.service;

import com.aiagent.common.model.LoadedContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.*;

@Service
public class ContextLoaderService {

    private static final Logger log = LoggerFactory.getLogger(ContextLoaderService.class);
    
    @Value("${mcp.server.url:http://localhost:8081}")
    private String mcpServerUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();

    public List<LoadedContext> loadContext(String task) {
        if (task == null || task.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            // For now, create minimal mock context based on task keywords
            // In real implementation, this would call MCP server context endpoint
            return createMockContext(task);
            
        } catch (Exception e) {
            log.warn("Failed to load context for task: {}", task, e);
            return Collections.emptyList();
        }
    }

    private List<LoadedContext> createMockContext(String task) {
        List<LoadedContext> contexts = new ArrayList<>();
        String lowerTask = task.toLowerCase();
        
        // Simple keyword-based context loading
        if (lowerTask.contains("userservice") || lowerTask.contains("user")) {
            contexts.add(new LoadedContext("UserService.java", "Primary symbol match"));
        }
        
        if (lowerTask.contains("caching") || lowerTask.contains("cache")) {
            contexts.add(new LoadedContext("CacheService.java", "Dependency match"));
        }
        
        if (lowerTask.contains("service") && !contexts.isEmpty()) {
            contexts.add(new LoadedContext("ServiceConfig.java", "Configuration match"));
        }
        
        // Add generic context for any task
        if (contexts.isEmpty()) {
            contexts.add(new LoadedContext("ApplicationContext.java", "Default context"));
        }
        
        log.info("Loaded {} context files for task: {}", contexts.size(), task);
        return contexts;
    }
}