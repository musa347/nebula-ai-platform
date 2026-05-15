package com.aiagent.common.model;

public class ContextSlice {
    private String file;
    private String content;
    private String reason;

    public ContextSlice() {}

    public ContextSlice(String file, String content, String reason) {
        this.file = file;
        this.content = content;
        this.reason = reason;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
