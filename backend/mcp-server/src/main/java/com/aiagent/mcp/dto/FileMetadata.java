package com.aiagent.mcp.dto;

public  class FileMetadata {
    private final String path;
    private final long size;
    private final long lastModified;
    private final boolean isReadable;

    private FileMetadata(Builder builder) {
        this.path = builder.path;
        this.size = builder.size;
        this.lastModified = builder.lastModified;
        this.isReadable = builder.isReadable;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public String getPath() { return path; }
    public long getSize() { return size; }
    public long getLastModified() { return lastModified; }
    public boolean isReadable() { return isReadable; }

    public static class Builder {
        private String path;
        private long size;
        private long lastModified;
        private boolean isReadable;

        public Builder path(String path) { this.path = path; return this; }
        public Builder size(long size) { this.size = size; return this; }
        public Builder lastModified(long lastModified) { this.lastModified = lastModified; return this; }
        public Builder isReadable(boolean isReadable) { this.isReadable = isReadable; return this; }

        public FileMetadata build() {
            return new FileMetadata(this);
        }
    }
}