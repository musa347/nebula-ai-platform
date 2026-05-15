package com.aiagent.mcp.service;

import com.aiagent.mcp.dto.PatchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.Patch;
import com.github.difflib.UnifiedDiffUtils;

/**
 * Production-safe filesystem patch service with backup creation and atomic writes.
 */
@Service
public class FilesystemPatchService {
    
    @Value("${mcp.allowed-directories}")
    private List<String> allowedDirectories;
    
    @Value("${mcp.max-file-size:1048576}") // 1MB default
    private long maxFileSize;
    
    @Value("${orchestrator.url:http://localhost:8082}")
    private String orchestratorUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    private static final Pattern UNIFIED_DIFF_PATTERN = Pattern.compile(
        "@@ -(\\d+)(?:,(\\d+))? \\+(\\d+)(?:,(\\d+))? @@"
    );

    public PatchResponse applyPatch(String filePath, String patchContent) throws IOException {
        return applyPatch(filePath, patchContent, null);
    }

    public PatchResponse applyPatch(String filePath, String patchContent, String executionId) throws IOException {
        Path normalizedPath = normalizeAndValidatePath(filePath);

        String backupPath = createBackup(normalizedPath);
        
        try {
            String originalContent = FileUtils.readFileToString(normalizedPath.toFile(), StandardCharsets.UTF_8);
            List<String> originalLines = List.of(originalContent.split("\\r?\\n"));

            Patch<String> patch = parsePatch(patchContent, originalLines.size());

            List<String> patchedLines = (List<String>) patch.applyTo(originalLines);

            String diff = generateUnifiedDiff(originalLines, patchedLines, filePath);

            writeAtomically(normalizedPath, String.join("\n", patchedLines));
            
            if (executionId != null) {
                notifyGraphService(executionId, "PATCH", "Applied patch to " + filePath);
            }
            
            return PatchResponse.success(diff, true, backupPath);
            
        } catch (Exception e) {
            try {
                Path backupFile = Paths.get(backupPath);
                if (Files.exists(backupFile)) {
                    FileUtils.copyFile(backupFile.toFile(), normalizedPath.toFile());
                }
            } catch (IOException restoreException) {
                System.err.println("Failed to restore backup: " + restoreException.getMessage());
            }
            throw new IOException("Patch application failed: " + e.getMessage(), e);
        }
    }

    private String createBackup(Path filePath) throws IOException {
        Path backupPath = Paths.get(filePath.toString() + ".bak");

        if (Files.exists(backupPath)) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            backupPath = Paths.get(filePath.toString() + ".bak." + timestamp);
        }
        
        FileUtils.copyFile(filePath.toFile(), backupPath.toFile());
        return backupPath.toString();
    }
    

    private Patch<String> parsePatch(String patchContent, int originalLineCount) throws IOException {
        if (!StringUtils.hasText(patchContent)) {
            throw new IOException("Patch content cannot be null or empty");
        }
        
        try {
            validatePatchFormat(patchContent, originalLineCount);

            List<String> patchLines = List.of(patchContent.split("\\r?\\n"));
            return UnifiedDiffUtils.parseUnifiedDiff(patchLines);
            
        } catch (Exception e) {
            throw new IOException("Failed to parse patch: " + e.getMessage(), e);
        }
    }

    private void validatePatchFormat(String patchContent, int originalLineCount) throws IOException {
        String[] lines = patchContent.split("\\r?\\n");
        boolean inPatch = false;
        
        for (String line : lines) {
            if (line.startsWith("@@")) {
                inPatch = true;
                Matcher matcher = UNIFIED_DIFF_PATTERN.matcher(line);
                if (!matcher.find()) {
                    throw new IOException("Invalid patch header format: " + line);
                }
                
                int oldStart = Integer.parseInt(matcher.group(1));
                int oldLines = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 1;

                if (oldStart < 1) {
                    throw new IOException("Invalid line reference: " + oldStart + " (must be >= 1)");
                }
                
                if (oldStart + oldLines - 1 > originalLineCount) {
                    throw new IOException("Patch references line " + (oldStart + oldLines - 1) + 
                                      " but file only has " + originalLineCount + " lines");
                }
            }
        }
        
        if (!inPatch) {
            throw new IOException("No valid patch hunks found in patch content");
        }
    }

    private String generateUnifiedDiff(List<String> originalLines, List<String> patchedLines, String filePath) {
        Patch<String> diff = DiffUtils.diff(originalLines, patchedLines);
        List<String> diffLines = UnifiedDiffUtils.generateUnifiedDiff(
            filePath, 
            filePath, 
            originalLines, 
            diff, 
            3
        );
        return String.join("\n", diffLines);
    }

    private void writeAtomically(Path filePath, String content) throws IOException {
        Path tempPath = Paths.get(filePath.toString() + ".tmp." + System.currentTimeMillis());
        
        try {
            Files.writeString(tempPath, content, StandardCharsets.UTF_8, 
                            StandardOpenOption.CREATE, 
                            StandardOpenOption.TRUNCATE_EXISTING);

            if (Files.size(tempPath) > maxFileSize) {
                throw new IOException("Patched file size exceeds maximum allowed size");
            }

            Files.move(tempPath, filePath, StandardCopyOption.REPLACE_EXISTING, 
                      StandardCopyOption.ATOMIC_MOVE);
            
        } catch (IOException e) {
            try {
                Files.deleteIfExists(tempPath);
            } catch (IOException cleanupException) {
                System.err.println("Failed to clean up temp file: " + cleanupException.getMessage());
            }
            throw e;
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
                    return finalNormalizedPath.startsWith(allowedPath) || 
                           finalNormalizedPath.toAbsolutePath().startsWith(allowedPath.toAbsolutePath());
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
        
        if (!Files.isWritable(normalizedPath)) {
            throw new IOException("File is not writable: " + normalizedPath);
        }
        
        return normalizedPath;
    }
    private boolean containsTraversal(Path path) {
        String pathString = path.toString();
        return pathString.contains("../") || 
               pathString.contains("..\\") ||
               pathString.contains("..") && pathString.indexOf("..") != pathString.lastIndexOf("..");
    }
    
    private void notifyGraphService(String executionId, String type, String message) {
        try {
            String url = orchestratorUrl + "/api/v1/orchestrator/graph/node";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, String> body = Map.of(
                "executionId", executionId,
                "type", type,
                "message", message
            );
            restTemplate.postForEntity(url, new HttpEntity<>(body, headers), Void.class);
        } catch (Exception e) {
            // Silent fail - graph tracking is optional
        }
    }
}
