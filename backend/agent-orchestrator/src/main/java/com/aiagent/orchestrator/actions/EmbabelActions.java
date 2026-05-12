package com.aiagent.orchestrator.actions;

import com.aiagent.common.dto.ShellExecuteRequest;
import com.aiagent.common.dto.ShellExecuteResponse;
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

    public String executeShell(String command) {
        ShellExecuteRequest request = new ShellExecuteRequest();
        request.setCommand(command);
        request.setWorkingDirectory("/tmp");
            
        ShellExecuteResponse response = mcpWebClient.post()
            .uri("/api/tools/shell/execute")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(ShellExecuteResponse.class)
            .block();
            
        if (response != null && response.isSuccess()) {
            return "SUCCESS";
        } else {
            String output = "";
            if (response != null) {
                if (response.getStderr() != null && !response.getStderr().isEmpty()) {
                    output += response.getStderr();
                }
                if (response.getStdout() != null && !response.getStdout().isEmpty()) {
                    output += response.getStdout();
                }
            }
            return output.isEmpty() ? "Command failed with exit code " + (response != null ? response.getExitCode() : "unknown") : output;
        }
    }

    public boolean applyPatch(String patch) {
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
