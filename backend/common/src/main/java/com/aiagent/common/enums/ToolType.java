package com.aiagent.common.enums;

public enum ToolType {
    REPO_SEARCH("repo.search"),
    REPO_GREP("repo.grep"),
    SYMBOL_SEARCH("symbol.search"),
    DEPENDENCY_ANALYSIS("dependency.analysis"),
    CONTEXT_LOAD("context.load"),
    FILE_READ("file.read"),
    PATCH_GENERATE("patch.generate"),
    PATCH_APPLY("patch.apply"),
    SHELL_EXECUTE("shell.execute"),
    NONE("none"),
    
    // Legacy compatibility
    FILESYSTEM_READ("filesystem.read"),
    FILESYSTEM_WRITE("filesystem.write"),
    FILESYSTEM_PATCH("filesystem.patch"),
    GIT_STATUS("git.status"),
    GIT_DIFF("git.diff"),
    GIT_RESTORE("git.restore");
    
    private final String toolName;
    
    ToolType(String toolName) {
        this.toolName = toolName;
    }
    
    public String getToolName() {
        return toolName;
    }
}