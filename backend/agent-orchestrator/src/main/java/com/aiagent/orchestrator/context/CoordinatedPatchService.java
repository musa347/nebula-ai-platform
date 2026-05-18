package com.aiagent.orchestrator.context;

import com.aiagent.common.model.PatchProposal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CoordinatedPatchService {
    private static final Logger log = LoggerFactory.getLogger(CoordinatedPatchService.class);
    private static final int MAX_COORDINATED_PATCHES = 3;
    
    public List<PatchProposal> coordinatePatches(PatchProposal primaryPatch, List<RelatedContext> relatedFiles) {
        List<PatchProposal> patches = new ArrayList<>();
        patches.add(primaryPatch);
        
        if (relatedFiles == null || relatedFiles.isEmpty()) {
            return patches;
        }
        
        Set<String> included = new HashSet<>();
        included.add(primaryPatch.getFile());
        
        for (RelatedContext related : relatedFiles) {
            if (patches.size() >= MAX_COORDINATED_PATCHES) {
                break;
            }
            
            if (shouldIncludeInPatch(primaryPatch, related) && !included.contains(related.getFile())) {
                PatchProposal relatedPatch = new PatchProposal();
                relatedPatch.setFile(related.getFile());
                relatedPatch.setDescription("Coordinated change for " + related.getRelationType());
                relatedPatch.setSuggestedChange("// Coordinated update");
                patches.add(relatedPatch);
                included.add(related.getFile());
            }
        }
        
        log.info("Coordinated {} patches for {}", patches.size(), primaryPatch.getFile());
        return patches;
    }
    
    private boolean shouldIncludeInPatch(PatchProposal primaryPatch, RelatedContext related) {
        String primaryFile = primaryPatch.getFile();
        String relatedFile = related.getFile();
        
        // Same package only
        if (!isSamePackage(primaryFile, relatedFile)) {
            return false;
        }
        
        // Include if cache-related
        if (primaryFile.contains("Cache") || relatedFile.contains("Cache")) {
            return related.getRelationType().equals("FIELD") || related.getRelationType().equals("INJECTION");
        }
        
        // Include if DTO/mapper related
        if (primaryFile.contains("DTO") || relatedFile.contains("Mapper")) {
            return true;
        }
        
        // Include if interface/implementation
        if (relatedFile.contains("Repository") || relatedFile.contains("Service")) {
            return related.getRelationType().equals("INJECTION");
        }
        
        return false;
    }
    
    private boolean isSamePackage(String file1, String file2) {
        // Simple heuristic: same module/package
        return true; // Simplified for now
    }
}
