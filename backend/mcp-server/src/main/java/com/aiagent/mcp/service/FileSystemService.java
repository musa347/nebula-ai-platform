package com.aiagent.mcp.service;

import com.aiagent.mcp.dto.FileMetadata;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;

/**
 * Production-safe file system service for reading files with security validations.
 */
@Service
public class FileSystemService {
    
    @Value("${mcp.allowed-directories}")
    private List<String> allowedDirectories;
    
    @Value("${mcp.max-file-size:1048576}") // 1MB default
    private long maxFileSize;

    public String readFile(String path) throws IOException {
        Path normalizedPath = normalizeAndValidatePath(path);
        

        long fileSize = Files.size(normalizedPath);
        if (fileSize > maxFileSize) {
            throw new IOException("File size " + fileSize + " exceeds maximum allowed size of " + maxFileSize);
        }

        try {
            return Files.readString(normalizedPath, StandardCharsets.UTF_8);
        } catch (MalformedInputException e) {
            throw new IOException("File contains invalid UTF-8 characters", e);
        }
    }

    private Path normalizeAndValidatePath(String path) throws IOException {
        if (!StringUtils.hasText(path)) {
            throw new IOException("Path cannot be null or empty");
        }
        

        Path normalizedPath = Paths.get(path).normalize();
        

        if (!normalizedPath.isAbsolute()) {
            normalizedPath = Paths.get(System.getProperty("user.dir")).resolve(normalizedPath).normalize();
        }

        if (containsTraversal(normalizedPath)) {
            throw new IOException("Path traversal detected: " + path);
        }

        final Path finalNormalizedPath = normalizedPath;
        
        boolean isAllowed = allowedDirectories.stream()
                .anyMatch(allowedDir -> {
                    Path allowedPath = Paths.get(allowedDir).normalize();
                    System.err.println("DEBUG: Checking if " + finalNormalizedPath + " starts with " + allowedPath);
                    boolean startsWith = finalNormalizedPath.startsWith(allowedPath) || 
                                       finalNormalizedPath.toAbsolutePath().startsWith(allowedPath.toAbsolutePath());
                    System.err.println("DEBUG: Result: " + startsWith);
                    return startsWith;
                });
        
        if (!isAllowed) {
            throw new IOException("Access denied. Path not in allowed directories: " + path);
        }

        if (!Files.exists(normalizedPath)) {
            throw new IOException("File does not exist: " + normalizedPath);
        }
        
        if (!Files.isRegularFile(normalizedPath)) {
            throw new IOException("Path is not a regular file: " + normalizedPath);
        }
        
        return normalizedPath;
    }

    private boolean containsTraversal(Path path) {
        String pathString = path.toString();
        return pathString.contains("../") || 
               pathString.contains("..\\") ||
               pathString.contains("..") && pathString.indexOf("..") != pathString.lastIndexOf("..");
    }

    public FileMetadata getFileMetadata(String path) throws IOException {
        Path normalizedPath = normalizeAndValidatePath(path);
        
        return FileMetadata.builder()
                .path(normalizedPath.toString())
                .size(Files.size(normalizedPath))
                .lastModified(Files.getLastModifiedTime(normalizedPath).toMillis())
                .isReadable(Files.isReadable(normalizedPath))
                .build();
    }
    


}
