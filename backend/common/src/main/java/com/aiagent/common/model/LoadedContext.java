package com.aiagent.common.model;

public class LoadedContext {
    private String file;
    private String reason;

    public LoadedContext() {}

    public LoadedContext(String file, String reason) {
        this.file = file;
        this.reason = reason;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}