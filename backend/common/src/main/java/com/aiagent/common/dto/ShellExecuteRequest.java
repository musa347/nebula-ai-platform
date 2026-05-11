package com.aiagent.common.dto;

/**
 * Request DTO for shell command execution.
 */
public class ShellExecuteRequest {

    private String command;

    private String workingDirectory;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }
}
