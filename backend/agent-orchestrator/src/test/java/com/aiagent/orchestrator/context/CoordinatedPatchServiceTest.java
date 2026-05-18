package com.aiagent.orchestrator.context;

import com.aiagent.common.model.PatchProposal;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CoordinatedPatchServiceTest {
    
    private final CoordinatedPatchService service = new CoordinatedPatchService();
    
    @Test
    void coordinatedPatchGeneration() {
        PatchProposal primary = new PatchProposal();
        primary.setFile("UserCacheService.java");
        
        List<RelatedContext> related = List.of(
            new RelatedContext("CacheConfig.java", "FIELD"),
            new RelatedContext("UserRepository.java", "INJECTION")
        );
        
        List<PatchProposal> patches = service.coordinatePatches(primary, related);
        
        assertTrue(patches.size() >= 1);
        assertEquals("UserCacheService.java", patches.get(0).getFile());
    }
    
    @Test
    void maxPatchLimitEnforced() {
        PatchProposal primary = new PatchProposal();
        primary.setFile("Service.java");
        
        List<RelatedContext> related = List.of(
            new RelatedContext("File1.java", "FIELD"),
            new RelatedContext("File2.java", "FIELD"),
            new RelatedContext("File3.java", "FIELD"),
            new RelatedContext("File4.java", "FIELD"),
            new RelatedContext("File5.java", "FIELD")
        );
        
        List<PatchProposal> patches = service.coordinatePatches(primary, related);
        
        assertTrue(patches.size() <= 3);
    }
    
    @Test
    void duplicatePrevention() {
        PatchProposal primary = new PatchProposal();
        primary.setFile("UserService.java");
        
        List<RelatedContext> related = List.of(
            new RelatedContext("UserService.java", "FIELD")
        );
        
        List<PatchProposal> patches = service.coordinatePatches(primary, related);
        
        assertEquals(1, patches.size());
    }
    
    @Test
    void unrelatedFileRejection() {
        PatchProposal primary = new PatchProposal();
        primary.setFile("UserService.java");
        
        List<RelatedContext> related = List.of();
        
        List<PatchProposal> patches = service.coordinatePatches(primary, related);
        
        assertEquals(1, patches.size());
    }
}
