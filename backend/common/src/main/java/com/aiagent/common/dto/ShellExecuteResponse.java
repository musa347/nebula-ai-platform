package com.aiagent.common.dto;

/**
 * Response DTO for shell command execution.
 */
public class ShellExecuteResponse {

    private boolean success;

    private String stdout;

    private String stderr;

    private Integer exitCode;

    private Long durationMs;

    public static Builder builder() {
        return new Builder();
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getStdout() {
        return stdout;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    public Integer getExitCode() {
        return exitCode;
    }

    public void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public static class Builder {
        private boolean success;
        private String stdout;
        private String stderr;
        private Integer exitCode;
        private Long durationMs;

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder stdout(String stdout) {
            this.stdout = stdout;
            return this;
        }

        public Builder stderr(String stderr) {
            this.stderr = stderr;
            return this;
        }

        public Builder exitCode(Integer exitCode) {
            this.exitCode = exitCode;
            return this;
        }

        public Builder durationMs(Long durationMs) {
            this.durationMs = durationMs;
            return this;
        }

        public ShellExecuteResponse build() {
            ShellExecuteResponse response = new ShellExecuteResponse();
            response.success = this.success;
            response.stdout = this.stdout;
            response.stderr = this.stderr;
            response.exitCode = this.exitCode;
            response.durationMs = this.durationMs;
            return response;
        }
    }
}
