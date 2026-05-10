package com.aiagent.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application class for the MCP (Model Context Protocol) Server.
 * 
 * This server acts as the execution runtime and tool gateway for the AI Agent Platform.
 * It provides:
 * - Filesystem operations (read, write, patch)
 * - Shell command execution
 * - Git operations
 * - Repository intelligence APIs
 * - Security enforcement and sandboxing
 * - WebSocket streaming support
 */
@SpringBootApplication(scanBasePackages = {
    "com.aiagent.mcp",
    "com.aiagent.common"
})
@EnableConfigurationProperties
@EnableAsync
public class McpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }
}
