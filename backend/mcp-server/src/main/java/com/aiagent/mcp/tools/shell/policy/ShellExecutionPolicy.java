package com.aiagent.mcp.tools.shell.policy;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShellExecutionPolicy {

    private final List<String> allowedCommands = List.of(
            "echo",
            "mvn",
            "gradle",
            "npm"
    );

    public boolean isAllowed(String command) {
        if (command == null || command.trim().isEmpty()) {
            return false;
        }
        
        String trimmedCommand = command.trim();
        return allowedCommands.stream()
                .anyMatch(trimmedCommand::startsWith);
    }

    public List<String> getAllowedCommands() {
        return List.copyOf(allowedCommands);
    }
}
