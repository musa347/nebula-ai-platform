package com.aiagent.common.enums;

public enum ToolType {
    
    // Types of tools available in the MCP server
    FILESYSTEM_READ("filesystem.read"),
    FILESYSTEM_WRITE("filesystem.write"),
    FILESYSTEM_PATCH("filesystem.patch"),
    FILESYSTEM_LIST("filesystem.list"),
    FILESYSTEM_DELETE("filesystem.delete"),
    SHELL_EXECUTE("shell.execute"),
    SHELL_BACKGROUND("shell.background"),
    GIT_DIFF("git.diff"),
    GIT_RESTORE("git.restore"),
    GIT_STATUS("git.status"),
    GIT_COMMIT("git.commit"),
    GIT_CHECKOUT("git.checkout"),
    GIT_LOG("git.log"),
    REPO_FIND_SYMBOL("repo.find_symbol"),
    REPO_FIND_REFERENCES("repo.find_references"),
    REPO_ANALYZE_STRUCTURE("repo.analyze_structure"),
    REPO_SEARCH_CODE("repo.search_code"),
    PATCH_APPLY("patch.apply"),
    PATCH_VALIDATE("patch.validate"),
    PATCH_REVERT("patch.revert");
    
    private final String toolName;
    
    ToolType(String toolName) {
        this.toolName = toolName;
    }
    
    public String getToolName() {
        return toolName;
    }
    

    public static ToolType fromToolName(String toolName) {
        for (ToolType type : values()) {
            if (type.toolName.equals(toolName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown tool name: " + toolName);
    }

    public boolean isFilesystemTool() {
        return name().startsWith("FILESYSTEM_");
    }

    public boolean isShellTool() {
        return name().startsWith("SHELL_");
    }

    public boolean isGitTool() {
        return name().startsWith("GIT_");
    }

    public boolean isRepositoryTool() {
        return name().startsWith("REPO_");
    }

    public boolean isPatchTool() {
        return name().startsWith("PATCH_");
    }
}
