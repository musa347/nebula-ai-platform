package com.aiagent.cli;

import com.aiagent.common.dto.OrchestratorTaskRequest;
import com.aiagent.common.dto.OrchestratorTaskResponse;
import com.aiagent.orchestrator.autonomous.AutonomousOrchestrator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

@Service
public class InteractiveAgentSession {
    
    @Autowired
    private AutonomousOrchestrator orchestrator;
    
    @Autowired
    private ExecutionTracePrinter tracePrinter;
    
    @Autowired
    private WorkspaceContextService workspaceService;
    
    private Map<String, OrchestratorTaskResponse> executionHistory;
    private WorkspaceContext workspaceContext;
    
    public InteractiveAgentSession() {
        this.executionHistory = new HashMap<>();
    }
    
    public void start(String workspacePath) {
        System.out.println("\n=== AI AGENT INTERACTIVE MODE ===\n");
        
        // Load workspace context
        if (workspacePath != null) {
            workspaceContext = workspaceService.scan(workspacePath);
            System.out.println("Workspace: " + workspaceContext.getRootPath());
            System.out.println("Build tool: " + workspaceContext.getBuildTool());
            System.out.println("Source files: " + workspaceContext.getSourceFiles().size());
            System.out.println("Modules: " + workspaceContext.getModules());
            System.out.println();
        }
        
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.print("agent> ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                continue;
            }
            
            if ("exit".equalsIgnoreCase(input) || "quit".equalsIgnoreCase(input)) {
                System.out.println("Goodbye!");
                break;
            }
            
            if ("help".equalsIgnoreCase(input)) {
                printHelp();
                continue;
            }
            
            if (input.startsWith("history")) {
                printHistory();
                continue;
            }
            
            // Execute task
            executeTask(input);
        }
        
        scanner.close();
    }
    
    private void executeTask(String task) {
        System.out.println();
        
        OrchestratorTaskRequest request = new OrchestratorTaskRequest(task);
        OrchestratorTaskResponse response = orchestrator.execute(request);
        
        // Store in history
        executionHistory.put(response.getExecutionId(), response);
        
        // Print trace
        tracePrinter.print(response);
        
        // Print result
        boolean success = response.getCompletedStates().contains(
            com.aiagent.common.enums.ExecutionState.COMPLETED);
        
        if (success) {
            System.out.println("✓ SUCCESS (id=" + response.getExecutionId() + ")");
        } else {
            System.out.println("✗ FAILED (id=" + response.getExecutionId() + ")");
        }
        
        System.out.println();
    }
    
    private void printHelp() {
        System.out.println("\nAvailable commands:");
        System.out.println("  <task>     - Execute a task");
        System.out.println("  history    - Show execution history");
        System.out.println("  help       - Show this help");
        System.out.println("  exit/quit  - Exit interactive mode");
        System.out.println();
    }
    
    private void printHistory() {
        System.out.println("\nExecution History:");
        
        if (executionHistory.isEmpty()) {
            System.out.println("  No executions yet");
        } else {
            executionHistory.forEach((id, response) -> {
                boolean success = response.getCompletedStates().contains(
                    com.aiagent.common.enums.ExecutionState.COMPLETED);
                String status = success ? "✓" : "✗";
                System.out.println("  " + status + " " + id + " - " + response.getCompletedStates().size() + " states");
            });
        }
        
        System.out.println();
    }
}
