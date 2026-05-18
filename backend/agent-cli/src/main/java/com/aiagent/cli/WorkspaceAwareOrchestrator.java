package com.aiagent.cli;

import com.aiagent.common.dto.OrchestratorTaskRequest;
import com.aiagent.common.dto.OrchestratorTaskResponse;
import com.aiagent.orchestrator.autonomous.AutonomousOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WorkspaceAwareOrchestrator {
    private static final Logger log = LoggerFactory.getLogger(WorkspaceAwareOrchestrator.class);
    
    @Autowired
    private AutonomousOrchestrator orchestrator;
    
    @Autowired
    private WorkspaceContextService workspaceService;
    
    private WorkspaceContext cachedContext;
    
    public OrchestratorTaskResponse execute(OrchestratorTaskRequest request, String workspacePath) {
        // Load workspace context if not cached
        if (cachedContext == null && workspacePath != null) {
            cachedContext = workspaceService.scan(workspacePath);
            log.info("Loaded workspace context: {} source files, {} modules",
                cachedContext.getSourceFiles().size(), cachedContext.getModules().size());
        }
        
        // Enrich task with workspace context
        String enrichedTask = enrichTaskWithContext(request.getTask());
        OrchestratorTaskRequest enrichedRequest = new OrchestratorTaskRequest(enrichedTask);
        enrichedRequest.setTargetFile(request.getTargetFile());
        
        return orchestrator.execute(enrichedRequest);
    }
    
    private String enrichTaskWithContext(String task) {
        if (cachedContext == null) {
            return task;
        }
        
        StringBuilder enriched = new StringBuilder(task);
        
        // Add workspace context hints
        enriched.append("\n\nWorkspace Context:");
        enriched.append("\n- Build tool: ").append(cachedContext.getBuildTool());
        enriched.append("\n- Modules: ").append(cachedContext.getModules());
        enriched.append("\n- Source files: ").append(cachedContext.getSourceFiles().size());
        
        return enriched.toString();
    }
    
    public void clearCache() {
        cachedContext = null;
    }
}
