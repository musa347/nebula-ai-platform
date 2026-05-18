package com.aiagent.orchestrator.context;

import com.aiagent.common.model.DependencyEdge;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiMultiFilePromptService {
    
    private static final String MULTI_FILE_TEMPLATE = """
            TASK:
            %s
            
            PRIMARY FILE:
            %s
            
            RELATED FILES:
            %s
            
            DEPENDENCY RELATIONSHIPS:
            %s
            
            Rules:
            - Maintain consistency across files
            - Avoid breaking dependencies
            - Avoid duplicate logic
            - Preserve interfaces
            
            Generate coordinated changes if needed.
            """;
    
    public String buildPrompt(String task, String primaryFile, List<RelatedContext> relatedFiles, List<DependencyEdge> dependencies) {
        String relatedSection = formatRelatedFiles(relatedFiles);
        String dependencySection = formatDependencies(dependencies);
        
        return String.format(MULTI_FILE_TEMPLATE, 
            task != null ? task : "No task specified",
            primaryFile != null ? primaryFile : "No primary file",
            relatedSection,
            dependencySection
        );
    }
    
    private String formatRelatedFiles(List<RelatedContext> relatedFiles) {
        if (relatedFiles == null || relatedFiles.isEmpty()) {
            return "None";
        }
        
        StringBuilder sb = new StringBuilder();
        for (RelatedContext related : relatedFiles) {
            sb.append("- ").append(related.getFile())
              .append(" (").append(related.getRelationType()).append(")\n");
        }
        return sb.toString();
    }
    
    private String formatDependencies(List<DependencyEdge> dependencies) {
        if (dependencies == null || dependencies.isEmpty()) {
            return "None";
        }
        
        StringBuilder sb = new StringBuilder();
        for (DependencyEdge edge : dependencies) {
            sb.append(edge.getFromClass()).append(" -> ")
              .append(edge.getToClass()).append(" (")
              .append(edge.getType()).append(")\n");
        }
        return sb.toString();
    }
}
