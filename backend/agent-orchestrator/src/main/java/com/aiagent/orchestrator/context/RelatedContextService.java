package com.aiagent.orchestrator.context;

import com.aiagent.common.model.DependencyEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RelatedContextService {
    private static final Logger log = LoggerFactory.getLogger(RelatedContextService.class);
    private static final int MAX_RELATED_FILES = 5;
    
    public List<RelatedContext> expandContext(String primaryFile, List<DependencyEdge> dependencies) {
        if (dependencies == null || dependencies.isEmpty()) {
            log.debug("No dependencies for file: {}", primaryFile);
            return List.of();
        }
        
        List<RelatedContext> related = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        
        for (DependencyEdge edge : dependencies) {
            if (related.size() >= MAX_RELATED_FILES) {
                break;
            }
            
            String relatedFile = edge.getToClass();
            if (!seen.contains(relatedFile) && !relatedFile.equals(primaryFile)) {
                related.add(new RelatedContext(relatedFile, edge.getType()));
                seen.add(relatedFile);
            }
        }
        
        log.info("Expanded context for {}: {} related files", primaryFile, related.size());
        return related;
    }
}
