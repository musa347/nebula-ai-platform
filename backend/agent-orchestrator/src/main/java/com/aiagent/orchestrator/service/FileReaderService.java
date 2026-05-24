package com.aiagent.orchestrator.service;

import com.aiagent.common.model.FilePreview;
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
public class FileReaderService {

    private static final Logger log = LoggerFactory.getLogger(FileReaderService.class);
    private static final int PREVIEW_LINES = 15;

    @Value("${mcp.server.url:http://localhost:8081}")
    private String mcpServerUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<FilePreview> readFilePreviews(List<LoadedContext> contexts) {
        return readFilePreviews(contexts, null);
    }

    public List<FilePreview> readFilePreviews(List<LoadedContext> contexts, String workspacePath) {
        List<FilePreview> previews = new ArrayList<>();

        if (contexts == null || contexts.isEmpty()) {
            log.warn("No contexts provided to readFilePreviews");
            return previews;
        }

        log.info("Reading {} file previews with workspace path: {}", contexts.size(), workspacePath);

        for (LoadedContext context : contexts) {
            try {
                String filePath = context.getFile();
                log.info("Processing file: {} (relative: {})", filePath, !filePath.startsWith("/"));

                if (workspacePath != null && !filePath.startsWith("/")) {
                    filePath = workspacePath + "/" + filePath;
                    log.info("Converted to absolute path: {}", filePath);
                }

                String preview = readFilePreview(filePath);
                if (preview != null) {
                    previews.add(new FilePreview(context.getFile(), preview));
                    log.info("Successfully read file preview for: {}", context.getFile());
                } else {
                    log.warn("Failed to read preview for: {}", filePath);
                }
            } catch (Exception e) {
                log.warn("Failed to read file preview for: {}", context.getFile(), e);
            }
        }

        log.info("Generated {} file previews from {} contexts", previews.size(), contexts.size());
        return previews;
    }

    private String readFilePreview(String filePath) {
        try {
            String url = mcpServerUrl + "/api/tools/execute";

            Map<String, Object> request = new HashMap<>();
            request.put("toolName", "filesystem.read");
            request.put("toolType", "FILESYSTEM_READ");
            request.put("parameters", Map.of("path", filePath));

            log.info("Calling MCP server: {} with path: {}", url, filePath);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);

            log.info("MCP server response: {}", response);

            if (response != null && "SUCCESS".equals(response.get("status"))) {
                @SuppressWarnings("unchecked")
                Map<String, Object> result = (Map<String, Object>) response.get("result");
                if (result != null) {
                    String content = (String) result.get("content");
                    log.info("Successfully read file, content length: {}", content != null ? content.length() : 0);
                    return content;
                }
            }

            log.warn("Failed to read file from MCP server: {}, response: {}", filePath, response);
            return null;

        } catch (Exception e) {
            log.error("Exception reading file: {}", filePath, e);
            return null;
        }
    }

}