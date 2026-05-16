package com.aiagent.common.model;

public class FileBackup {
    private String file;
    private String backupPath;

    public FileBackup() {}

    public FileBackup(String file, String backupPath) {
        this.file = file;
        this.backupPath = backupPath;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getBackupPath() {
        return backupPath;
    }

    public void setBackupPath(String backupPath) {
        this.backupPath = backupPath;
    }
}