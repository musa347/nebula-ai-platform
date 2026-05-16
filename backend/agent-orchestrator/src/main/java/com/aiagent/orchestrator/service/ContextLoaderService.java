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
            return createMockContextWithTarget(task, null);
            
        } catch (Exception e) {
            log.warn("Failed to load context for task: {}", task, e);
            return Collections.emptyList();
        }
    }

    public List<LoadedContext> loadContext(String task, String targetFile) {
        if (task == null || task.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            return createMockContextWithTarget(task, targetFile);
        } catch (Exception e) {
            log.warn("Failed to load context for task: {} with target: {}", task, targetFile, e);
            return Collections.emptyList();
        }
    }

    private List<LoadedContext> createMockContextWithTarget(String task, String targetFile) {
        List<LoadedContext> contexts = new ArrayList<>();
        String lowerTask = task.toLowerCase();
        
        // Always include the target file if specified
        if (targetFile != null && !targetFile.trim().isEmpty()) {
            contexts.add(new LoadedContext(targetFile, "Target file"));
        }
        
        // Add additional context based on task keywords
        if (lowerTask.contains("userservice") || lowerTask.contains("user")) {
            contexts.add(new LoadedContext("UserService.java", "Primary symbol match"));
        }
        
        if (lowerTask.contains("caching") || lowerTask.contains("cache")) {
            contexts.add(new LoadedContext("CacheService.java", "Dependency match"));
        }
        
        if (lowerTask.contains("logging") || lowerTask.contains("log")) {
            contexts.add(new LoadedContext("LoggingConfig.java", "Logging context"));
        }
        
        if (lowerTask.contains("service") && contexts.size() == 1) {
            contexts.add(new LoadedContext("ServiceConfig.java", "Configuration match"));
        }
        
        // Ensure we always have at least one context
        if (contexts.isEmpty()) {
            contexts.add(new LoadedContext("ApplicationContext.java", "Default context"));
        }
        
        log.info("Loaded {} context files for task: {} with target: {}", contexts.size(), task, targetFile);
        return contexts;
    }
}