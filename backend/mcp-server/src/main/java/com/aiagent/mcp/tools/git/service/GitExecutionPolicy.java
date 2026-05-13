package com.aiagent.mcp.tools.git.service;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Set;
@Component
public class GitExecutionPolicy {
    private static final Set<String> ALLOWED_COMMANDS = Set.of("git status", "git diff", "git restore", "git stash");
    private static final Set<String> BLOCKED_COMMANDS = Set.of("git reset --hard", "git clean -fdx", "git push --force");
    public boolean isAllowed(String command) {
        if (command == null || command.trim().isEmpty()) return false;
        String trimmed = command.trim();
        if (BLOCKED_COMMANDS.stream().anyMatch(trimmed::startsWith)) return false;
        return ALLOWED_COMMANDS.stream().anyMatch(trimmed::startsWith);
    }
    public void validateRestoreTarget(String target) {
        if (target == null) return;
        if (target.contains("..") || target.startsWith("/")) throw new SecurityException("Invalid restore target: " + target);
    }
    public List<String> getAllowedCommands() { return List.copyOf(ALLOWED_COMMANDS); }
}
