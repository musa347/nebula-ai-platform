package com.aiagent.orchestrator.service;

import com.aiagent.common.model.FileBackup;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class BackupService {
    
    private static final String BACKUP_DIR = "./.agent-backups/";
    
    public FileBackup createBackup(String file) {
        try {
            // Create backup directory if not exists
            Path backupDirPath = Paths.get(BACKUP_DIR);
            if (!Files.exists(backupDirPath)) {
                Files.createDirectories(backupDirPath);
            }
            
            // Generate backup filename
            String fileName = Paths.get(file).getFileName().toString();
            String timestamp = String.valueOf(System.currentTimeMillis());
            String backupFileName = fileName + "_" + timestamp + ".bak";
            String backupPath = BACKUP_DIR + backupFileName;
            
            // Read original file and create backup
            Path originalPath = Paths.get(file);
            Path backupFilePath = Paths.get(backupPath);
            
            if (Files.exists(originalPath)) {
                Files.copy(originalPath, backupFilePath);
            } else {
                // Create empty backup for non-existent files
                Files.createFile(backupFilePath);
            }
            
            return new FileBackup(file, backupPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create backup for file: " + file, e);
        }
    }
    
    public void restoreBackup(FileBackup backup) {
        try {
            Path backupPath = Paths.get(backup.getBackupPath());
            Path originalPath = Paths.get(backup.getFile());
            
            if (Files.exists(backupPath)) {
                // Ensure parent directory exists
                Path parentDir = originalPath.getParent();
                if (parentDir != null && !Files.exists(parentDir)) {
                    Files.createDirectories(parentDir);
                }
                
                Files.copy(backupPath, originalPath, 
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to restore backup: " + backup.getBackupPath(), e);
        }
    }
}