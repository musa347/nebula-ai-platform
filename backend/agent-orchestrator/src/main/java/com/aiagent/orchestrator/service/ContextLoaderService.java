package com.aiagent.orchestrator.service;

import com.aiagent.common.model.LoadedContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.*;

@Service
public class ContextLoaderService {

    private static final Logger log = LoggerFactory.getLogger(ContextLoaderService.class);

    @Value("${mcp.server.url:http://localhost:8081}")
    private String mcpServerUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<LoadedContext> loadContext(String task) {
        return loadContext(task, null, null);
    }

    public List<LoadedContext> loadContext(String task, String targetFile) {
        return loadContext(task, targetFile, null);
    }

    public List<LoadedContext> loadContext(String task, String targetFile, List<String> workspaceFiles) {
        if (task == null || task.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            return createContextWithWorkspace(task, targetFile, workspaceFiles);
        } catch (Exception e) {
            log.warn("Failed to load context for task: {} with target: {}", task, targetFile, e);
            return Collections.emptyList();
        }
    }

    private List<LoadedContext> createContextWithWorkspace(String task, String targetFile, List<String> workspaceFiles) {
        List<LoadedContext> contexts = new ArrayList<>();

        if (workspaceFiles != null && !workspaceFiles.isEmpty()) {
            Set<String> explicitFiles = extractFileNames(task);

            log.info("Extracted file names from task: {}", explicitFiles);
            log.info("Sample workspace files: {}", workspaceFiles.subList(0, Math.min(3, workspaceFiles.size())));

            for (String file : workspaceFiles) {
                String fileName = file.contains("/") ?
                        file.substring(file.lastIndexOf('/') + 1).toLowerCase() :
                        file.toLowerCase();
                String fileNameNoExt = fileName.replaceAll("\\.(java|kt|py|js|ts)$", "");

                for (String explicitFile : explicitFiles) {
                    String explicitLower = explicitFile.toLowerCase();
                    if (fileName.equals(explicitLower) ||
                            fileNameNoExt.equals(explicitLower) ||
                            fileName.contains(explicitLower)) {
                        contexts.add(new LoadedContext(file, "Explicit file match: " + explicitFile));
                        log.info("Matched file: {} with pattern: {}", file, explicitFile);
                        break;
                    }
                }
            }

            if (!contexts.isEmpty()) {
                log.info("Found {} matching files from workspace of {} files", contexts.size(), workspaceFiles.size());
                log.info("Loaded {} context files for task: {} with target: {}", contexts.size(), task, targetFile);
                return contexts;
            }

            String lowerTask = task.toLowerCase();
            for (String file : workspaceFiles) {
                String fileLower = file.toLowerCase();

                if (lowerTask.contains("endpoint") || lowerTask.contains("validation")) {
                    if (fileLower.contains("controller") || fileLower.contains("endpoint") ||
                            fileLower.contains("resource") || fileLower.contains("rest")) {
                        contexts.add(new LoadedContext(file, "Endpoint match"));
                    }
                }

                if (lowerTask.contains("service") && fileLower.contains("service")) {
                    contexts.add(new LoadedContext(file, "Service match"));
                }
            }

            log.info("Found {} matching files from workspace of {} files", contexts.size(), workspaceFiles.size());
        }

        if (targetFile != null && !targetFile.trim().isEmpty()) {
            contexts.add(0, new LoadedContext(targetFile, "Target file"));
        }

        log.info("Loaded {} context files for task: {} with target: {}", contexts.size(), task, targetFile);
        return contexts;
    }

    private Set<String> extractFileNames(String task) {
        Set<String> fileNames = new HashSet<>();
        if (task == null || task.trim().isEmpty()) {
            return fileNames;
        }

        String[] words = task.split("\\s+");
        for (String word : words) {
            String cleaned = word.replaceAll("[,;!?()\"']+$", "");
            if (cleaned.isEmpty()) continue;

            String lowerCleaned = cleaned.toLowerCase();

            if (lowerCleaned.matches(".*\\.(java|kt|py|js|ts)$")) {
                fileNames.add(lowerCleaned);
                fileNames.add(lowerCleaned.replaceAll("\\.(java|kt|py|js|ts)$", ""));
            } else if (cleaned.length() > 3 && Character.isUpperCase(cleaned.charAt(0))) {
                fileNames.add(lowerCleaned);
            }
        }

        return fileNames;
    }


}
