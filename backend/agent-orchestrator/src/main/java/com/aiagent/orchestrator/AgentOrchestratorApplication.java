package com.aiagent.orchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application class for the Agent Orchestrator.
 * 
 * This component serves as the main AI reasoning engine for the AI Agent Platform.
 * It provides:
 * - Planning and reasoning loop
 * - Prompt construction and model routing
 * - Task decomposition and context compression
 * - Memory management and retry handling
 * - Streaming responses via WebSocket
 * - Tool calling engine (communicates with MCP server)
 */
@SpringBootApplication(scanBasePackages = {
    "com.aiagent.orchestrator",
    "com.aiagent.common"
})
@EnableConfigurationProperties
@EnableAsync
public class AgentOrchestratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentOrchestratorApplication.class, args);
    }
}
