package com.aiagent.orchestrator.context;

import com.aiagent.common.model.DependencyEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MultiFileContextService {
    private static final Logger log = LoggerFactory.getLogger(MultiFileContextService.class);
    
    public String assembleContext(String primaryFile, List<RelatedContext> relatedFiles, List<DependencyEdge> dependencies) {
        StringBuilder context = new StringBuilder();
        
        context.append("PRIMARY FILE:\n");
        context.append(primaryFile).append("\n\n");
        
        if (relatedFiles != null && !relatedFiles.isEmpty()) {
            context.append("RELATED FILES:\n");
            for (RelatedContext related : relatedFiles) {
                context.append("- ").append(related.getFile())
                       .append(" (").append(related.getRelationType()).append(")\n");
            }
            context.append("\n");
        }
        
        if (dependencies != null && !dependencies.isEmpty()) {
            context.append("DEPENDENCY SUMMARY:\n");
            for (DependencyEdge edge : dependencies) {
                context.append(edge.getFromClass()).append(" -> ")
                       .append(edge.getToClass()).append(" (")
                       .append(edge.getType()).append(")\n");
            }
        }
        
        log.debug("Assembled multi-file context for {}", primaryFile);
        return context.toString();
    }
}
