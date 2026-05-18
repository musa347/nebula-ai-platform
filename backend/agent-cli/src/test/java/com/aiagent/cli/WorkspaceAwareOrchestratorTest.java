package com.aiagent.cli;

import com.aiagent.common.dto.OrchestratorTaskRequest;
import com.aiagent.common.dto.OrchestratorTaskResponse;
import com.aiagent.common.enums.ExecutionState;
import com.aiagent.orchestrator.autonomous.AutonomousOrchestrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WorkspaceAwareOrchestratorTest {
    
    private WorkspaceAwareOrchestrator orchestrator;
    private WorkspaceContextService workspaceService;
    private StubAutonomousOrchestrator stubOrchestrator;
    
    @BeforeEach
    void setUp() {
        workspaceService = new WorkspaceContextService();
        stubOrchestrator = new StubAutonomousOrchestrator();
        orchestrator = new WorkspaceAwareOrchestrator();
        
        injectDependencies();
    }
    
    @Test
    void testContextInjectedAutomatically(@TempDir Path tempDir) throws Exception {
        // Create workspace
        Files.createDirectories(tempDir.resolve("src/main/java"));
        Files.createFile(tempDir.resolve("pom.xml"));
        Files.createFile(tempDir.resolve("src/main/java/Service.java"));
        
        OrchestratorTaskRequest request = new OrchestratorTaskRequest("Test task");
        orchestrator.execute(request, tempDir.toString());
        
        // Verify context was loaded
        assertNotNull(stubOrchestrator.lastRequest);
        assertTrue(stubOrchestrator.lastRequest.getTask().contains("Workspace Context"));
        assertTrue(stubOrchestrator.lastRequest.getTask().contains("maven"));
    }
    
    @Test
    void testInfluencesPlanning(@TempDir Path tempDir) throws Exception {
        // Create multi-module workspace
        Files.createDirectories(tempDir.resolve("module1"));
        Files.createDirectories(tempDir.resolve("module2"));
        Files.createFile(tempDir.resolve("pom.xml"));
        Files.createFile(tempDir.resolve("module1/pom.xml"));
        Files.createFile(tempDir.resolve("module2/pom.xml"));
        
        OrchestratorTaskRequest request = new OrchestratorTaskRequest("Test task");
        orchestrator.execute(request, tempDir.toString());
        
        // Verify modules are mentioned
        assertTrue(stubOrchestrator.lastRequest.getTask().contains("module1"));
        assertTrue(stubOrchestrator.lastRequest.getTask().contains("module2"));
    }
    
    @Test
    void testImprovesSymbolSearchRelevance(@TempDir Path tempDir) throws Exception {
        Files.createDirectories(tempDir.resolve("src/main/java"));
        Files.createFile(tempDir.resolve("pom.xml"));
        
        OrchestratorTaskRequest request = new OrchestratorTaskRequest("Find UserService");
        orchestrator.execute(request, tempDir.toString());
        
        // Verify workspace info is included
        assertNotNull(stubOrchestrator.lastRequest);
        assertTrue(stubOrchestrator.lastRequest.getTask().contains("Source files"));
    }
    
    private void injectDependencies() {
        try {
            var orchestratorField = WorkspaceAwareOrchestrator.class.getDeclaredField("orchestrator");
            orchestratorField.setAccessible(true);
            orchestratorField.set(orchestrator, stubOrchestrator);
            
            var workspaceField = WorkspaceAwareOrchestrator.class.getDeclaredField("workspaceService");
            workspaceField.setAccessible(true);
            workspaceField.set(orchestrator, workspaceService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject dependencies", e);
        }
    }
    
    private static class StubAutonomousOrchestrator extends AutonomousOrchestrator {
        OrchestratorTaskRequest lastRequest;
        
        @Override
        public OrchestratorTaskResponse execute(OrchestratorTaskRequest request) {
            this.lastRequest = request;
            return new OrchestratorTaskResponse(
                "exec-123",
                List.of(ExecutionState.COMPLETED),
                List.of(),
                List.of(),
                List.of(),
                List.of()
            );
        }
    }
}
