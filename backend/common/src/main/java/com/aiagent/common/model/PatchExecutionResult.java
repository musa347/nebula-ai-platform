package com.aiagent.common.model;

public class PatchExecutionResult {
    private String file;
    private boolean success;
    private String message;
    private boolean reverted;
    private String backupPath;

    public PatchExecutionResult() {}

    public PatchExecutionResult(String file, boolean success, String message) {
        this.file = file;
        this.success = success;
        this.message = message;
        this.reverted = false;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isReverted() {
        return reverted;
    }

    public void setReverted(boolean reverted) {
        this.reverted = reverted;
    }

    public String getBackupPath() {
        return backupPath;
    }

    public void setBackupPath(String backupPath) {
        this.backupPath = backupPath;
    }
}