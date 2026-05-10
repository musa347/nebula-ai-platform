package com.aiagent.orchestrator.actions;

import com.aiagent.common.dto.ToolRequest;
import com.aiagent.common.dto.ToolResponse;
import com.aiagent.common.enums.ToolType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class EmbabelActions {
    
    private final WebClient mcpWebClient;

    public EmbabelActions(WebClient mcpWebClient) {
        this.mcpWebClient = mcpWebClient;
    }

    public String readFile(String filePath) {
        // Spring AI generates the plan, Embabel executes with type safety
        ToolRequest request = ToolRequest.builder()
            .toolName("filesystem.read")
            .toolType(ToolType.FILESYSTEM_READ)
            .parameters(Map.of("path", filePath))
            .build();
            
        return mcpWebClient.post()
            .uri("/api/tools/execute")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(String.class)
            .block();
    }
    
    /**
     * Execute shell command with safety checks
     */
    public String executeShell(String command) {
        // Spring AI determines command, Embabel provides type-safe execution
        ToolRequest request = ToolRequest.builder()
            .toolName("shell.execute")
            .toolType(ToolType.SHELL_EXECUTE)
            .parameters(Map.of("command", command))
            .build();
            
        return mcpWebClient.post()
            .uri("/api/tools/execute")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(String.class)
            .block();
    }
    
    /**
     * Apply code patch with validation
     */
    public boolean applyPatch(String patch) {
        // Spring AI generates patch, Embabel ensures type-safe application
        ToolRequest request = ToolRequest.builder()
            .toolName("patch.apply")
            .toolType(ToolType.PATCH_APPLY)
            .parameters(Map.of("patch", patch))
            .build();
            
        ToolResponse response = mcpWebClient.post()
            .uri("/api/tools/execute")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(ToolResponse.class)
            .block();
            
        return response.isSuccess();
    }
}
