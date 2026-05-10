package com.aiagent.common.constants;

/**
 * Constants for MCP (Model Context Protocol) operations.
 */
public final class McpConstants {
    
    private McpConstants() {
        // Utility class
    }
    
    // Protocol versions
    public static final String MCP_VERSION_1_0 = "1.0";
    public static final String DEFAULT_MCP_VERSION = MCP_VERSION_1_0;
    
    // Content types
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_TEXT = "text/plain";
    public static final String CONTENT_TYPE_PATCH = "application/patch";
    
    // HTTP headers
    public static final String HEADER_MCP_VERSION = "X-MCP-Version";
    public static final String HEADER_SESSION_ID = "X-Session-Id";
    public static final String HEADER_REQUEST_ID = "X-Request-Id";
    public static final String HEADER_TOOL_NAME = "X-Tool-Name";
    
    // WebSocket endpoints
    public static final String WS_ENDPOINT = "/ws/mcp";
    public static final String WS_TOOL_EXECUTION = "/ws/tool-execution";
    public static final String WS_STREAM_RESPONSE = "/ws/stream-response";
    
    // REST endpoints
    public static final String API_BASE = "/api/v1";
    public static final String API_TOOLS = API_BASE + "/tools";
    public static final String API_EXECUTE = API_TOOLS + "/execute";
    public static final String API_STATUS = API_TOOLS + "/status";
    public static final String API_CANCEL = API_TOOLS + "/cancel";
    
    // Tool categories
    public static final String CATEGORY_FILESYSTEM = "filesystem";
    public static final String CATEGORY_SHELL = "shell";
    public static final String CATEGORY_GIT = "git";
    public static final String CATEGORY_REPOSITORY = "repository";
    public static final String CATEGORY_PATCH = "patch";
    
    // Execution limits
    public static final long DEFAULT_TIMEOUT_MS = 30000; // 30 seconds
    public static final long MAX_TIMEOUT_MS = 300000; // 5 minutes
    public static final int MAX_CONCURRENT_EXECUTIONS = 10;
    
    // File size limits
    public static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10MB
    public static final int MAX_PATCH_SIZE_CHARS = 100000; // 100K characters
    
    // Security
    public static final String SANDBOX_WORK_DIR = "/tmp/mcp-sandbox";
    public static final String ALLOWED_PATHS_CONFIG = "mcp.security.allowed-paths";
    public static final String BLOCKED_COMMANDS_CONFIG = "mcp.security.blocked-commands";
    
    // Error codes
    public static final String ERROR_TOOL_NOT_FOUND = "TOOL_NOT_FOUND";
    public static final String ERROR_INVALID_PARAMETERS = "INVALID_PARAMETERS";
    public static final String ERROR_EXECUTION_TIMEOUT = "EXECUTION_TIMEOUT";
    public static final String ERROR_SECURITY_VIOLATION = "SECURITY_VIOLATION";
    public static final String ERROR_FILE_NOT_FOUND = "FILE_NOT_FOUND";
    public static final String ERROR_PERMISSION_DENIED = "PERMISSION_DENIED";
    public static final String ERROR_PATCH_FAILED = "PATCH_FAILED";
}
