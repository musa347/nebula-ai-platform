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
    private static final int PREVIEW_LINES = 15; // First 15 lines only
    
    @Value("${mcp.server.url:http://localhost:8081}")
    private String mcpServerUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();

    public List<FilePreview> readFilePreviews(List<LoadedContext> contexts) {
        List<FilePreview> previews = new ArrayList<>();
        
        if (contexts == null || contexts.isEmpty()) {
            return previews;
        }

        for (LoadedContext context : contexts) {
            try {
                String preview = readFilePreview(context.getFile());
                if (preview != null) {
                    previews.add(new FilePreview(context.getFile(), preview));
                }
            } catch (Exception e) {
                log.warn("Failed to read file preview for: {}", context.getFile(), e);
                // Continue execution - don't fail on individual file read errors
            }
        }
        
        log.info("Generated {} file previews from {} contexts", previews.size(), contexts.size());
        return previews;
    }

    private String readFilePreview(String filePath) {
        try {
            // For now, create mock file content since we don't have actual files
            // In real implementation, this would call MCP server filesystem.read endpoint
            return createMockFilePreview(filePath);
            
        } catch (Exception e) {
            log.warn("Failed to read file: {}", filePath, e);
            return null;
        }
    }

    private String createMockFilePreview(String filePath) {
        // Create realistic mock content based on file type
        if (filePath.endsWith(".java")) {
            String className = extractClassName(filePath);
            return String.format(
                "package com.example.service;\n\n" +
                "import org.springframework.stereotype.Service;\n" +
                "import java.util.List;\n\n" +
                "@Service\n" +
                "public class %s {\n" +
                "    \n" +
                "    public void processRequest() {\n" +
                "        // Implementation here\n" +
                "    }\n" +
                "    \n" +
                "    // Additional methods...\n" +
                "}\n",
                className
            );
        }
        
        if (filePath.endsWith(".xml") || filePath.endsWith(".config")) {
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                   "<configuration>\n" +
                   "    <settings>\n" +
                   "        <property name=\"example\">value</property>\n" +
                   "    </settings>\n" +
                   "</configuration>\n";
        }
        
        // Default preview
        return "# " + filePath + "\n" +
               "\n" +
               "This is a preview of the file content.\n" +
               "Only the first " + PREVIEW_LINES + " lines are shown.\n" +
               "\n" +
               "[Content truncated for brevity]\n";
    }

    private String extractClassName(String filePath) {
        String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
        return fileName.replace(".java", "");
    }
}