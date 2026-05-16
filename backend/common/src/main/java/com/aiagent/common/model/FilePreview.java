package com.aiagent.common.model;

public class FilePreview {
    private String file;
    private String preview;

    public FilePreview() {}

    public FilePreview(String file, String preview) {
        this.file = file;
        this.preview = preview;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getPreview() {
        return preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }
}