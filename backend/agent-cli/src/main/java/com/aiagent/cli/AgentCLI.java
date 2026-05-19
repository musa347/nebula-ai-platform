package com.aiagent.cli;

import com.aiagent.common.dto.OrchestratorTaskRequest;
import com.aiagent.common.dto.OrchestratorTaskResponse;
import com.aiagent.common.enums.ExecutionState;
import com.aiagent.orchestrator.autonomous.AutonomousOrchestrator;
import com.aiagent.orchestrator.streaming.ExecutionEventBus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.aiagent.cli", "com.aiagent.orchestrator"})
public class AgentCLI implements CommandLineRunner {
    
    @Autowired
    private AutonomousOrchestrator orchestrator;
    
    @Autowired
    private ExecutionTracePrinter tracePrinter;
    
    @Autowired
    private InteractiveAgentSession interactiveSession;
    
    @Autowired(required = false)
    private ExecutionEventBus eventBus;
    
    @Autowired
    private StreamingConsoleRenderer streamingRenderer;
    
    public static void main(String[] args) {
        SpringApplication.run(AgentCLI.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        if (args.length == 0) {
            printUsage();
            return;
        }
        
        String command = args[0];
        
        if ("run".equals(command)) {
            if (args.length < 2) {
                System.err.println("Error: Task description required");
                printUsage();
                return;
            }
            String task = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
            executeTask(task);
        } else if ("stream".equals(command)) {
            if (args.length < 2) {
                System.err.println("Error: Task description required");
                printUsage();
                return;
            }
            String task = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
            executeTaskStreaming(task);
        } else if ("interactive".equals(command)) {
            String workspacePath = args.length > 1 ? args[1] : System.getProperty("user.dir");
            interactiveSession.start(workspacePath);
        } else {
            System.err.println("Unknown command: " + command);
            printUsage();
        }
    }
    
    private void executeTaskStreaming(String task) {
        System.out.println("\n=== AI AGENT STREAMING EXECUTION ===\n");
        System.out.println("Task: " + task + "\n");
        
        // Subscribe to event stream
        if (eventBus != null) {
            streamingRenderer.start();
            eventBus.subscribe(streamingRenderer);
        }
        
        try {
            OrchestratorTaskRequest request = new OrchestratorTaskRequest(task);
            OrchestratorTaskResponse response = orchestrator.execute(request);
            
            System.out.println();
            printResult(response);
        } finally {
            // Unsubscribe
            if (eventBus != null) {
                eventBus.unsubscribe(streamingRenderer);
            }
        }
    }
    
    private void executeTask(String task) {
        System.out.println("\n=== AI AGENT EXECUTION ===\n");
        System.out.println("Task: " + task + "\n");
        
        OrchestratorTaskRequest request = new OrchestratorTaskRequest(task);
        OrchestratorTaskResponse response = orchestrator.execute(request);
        
        tracePrinter.print(response);
        
        printResult(response);
    }
    
    private void printResult(OrchestratorTaskResponse response) {
        System.out.println("\n[RESULT]");
        
        boolean success = response.getCompletedStates().contains(ExecutionState.COMPLETED);
        if (success) {
            System.out.println("✓ SUCCESS (executionId=" + response.getExecutionId() + ")");
        } else {
            System.out.println("✗ FAILED (executionId=" + response.getExecutionId() + ")");
        }
        
        System.out.println("\nStates: " + response.getCompletedStates());
        System.out.println("Patches: " + response.getPatches().size());
        System.out.println();
    }
    
    private void printUsage() {
        System.out.println("Usage:");
        System.out.println("  agent run \"<task description>\"");
        System.out.println("  agent stream \"<task description>\"");
        System.out.println("  agent interactive [workspace-path]");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  agent run \"Add caching to UserService\"");
        System.out.println("  agent stream \"Add caching to UserService\"");
        System.out.println("  agent interactive /path/to/project");
    }
}
