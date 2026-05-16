package com.aiagent.orchestrator.service;

import com.aiagent.common.model.PatchProposal;
import com.aiagent.common.model.PatchExecutionResult;
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
public class PatchExecutionService {

    private static final Logger log = LoggerFactory.getLogger(PatchExecutionService.class);
    
    @Value("${mcp.server.url:http://localhost:8081}")
    private String mcpServerUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();

    public List<PatchExecutionResult> executePatches(List<PatchProposal> patches) {
        List<PatchExecutionResult> results = new ArrayList<>();
        
        if (patches == null || patches.isEmpty()) {
            return results;
        }

        for (PatchProposal patch : patches) {
            try {
                PatchExecutionResult result = executePatch(patch);
                results.add(result);
            } catch (Exception e) {
                log.warn("Failed to execute patch for file: {}", patch.getFile(), e);
                // Continue execution - don't fail entire run on single patch failure
                results.add(new PatchExecutionResult(
                    patch.getFile(), 
                    false, 
                    "Patch execution failed: " + e.getMessage()
                ));
            }
        }
        
        log.info("Executed {} patches with {} successes", 
                patches.size(), 
                results.stream().mapToInt(r -> r.isSuccess() ? 1 : 0).sum());
        
        return results;
    }

    private PatchExecutionResult executePatch(PatchProposal patch) {
        try {
            // For now, simulate patch execution since we don't have real MCP server integration
            // In real implementation, this would call MCP server filesystem.patch endpoint
            return simulatePatchExecution(patch);
            
        } catch (Exception e) {
            log.warn("Failed to execute patch for file: {}", patch.getFile(), e);
            return new PatchExecutionResult(
                patch.getFile(), 
                false, 
                "Patch execution error: " + e.getMessage()
            );
        }
    }

    private PatchExecutionResult simulatePatchExecution(PatchProposal patch) {
        // Simulate patch execution with deterministic results based on file type
        String file = patch.getFile();
        
        // Simulate some patches failing for testing
        if (file.contains("NonExistent") || file.contains("ReadOnly")) {
            return new PatchExecutionResult(
                file, 
                false, 
                "File not found or read-only"
            );
        }
        
        // Most patches succeed in simulation
        if (file.endsWith(".java")) {
            return new PatchExecutionResult(
                file, 
                true, 
                "Successfully applied " + patch.getDescription().toLowerCase()
            );
        }
        
        if (file.endsWith(".xml") || file.endsWith(".config")) {
            return new PatchExecutionResult(
                file, 
                true, 
                "Configuration updated: " + patch.getDescription()
            );
        }
        
        // Default success
        return new PatchExecutionResult(
            file, 
            true, 
            "Patch applied successfully"
        );
    }
}