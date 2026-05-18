package com.aiagent.orchestrator.context;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ImpactAnalysisServiceTest {
    
    private final ImpactAnalysisService service = new ImpactAnalysisService();
    
    @Test
    void lowRiskDetection() {
        ImpactAnalysis analysis = service.analyzeImpact("UserService.java", List.of());
        
        assertEquals("LOW", analysis.getRiskLevel());
        assertEquals(1, analysis.getAffectedFiles().size());
    }
    
    @Test
    void mediumRiskDetection() {
        List<RelatedContext> related = List.of(
            new RelatedContext("UserRepository.java", "FIELD"),
            new RelatedContext("CacheService.java", "INJECTION")
        );
        
        ImpactAnalysis analysis = service.analyzeImpact("UserService.java", related);
        
        assertEquals("MEDIUM", analysis.getRiskLevel());
        assertEquals(3, analysis.getAffectedFiles().size());
    }
    
    @Test
    void highRiskDetection() {
        ImpactAnalysis analysis = service.analyzeImpact("UserInterface.java", List.of());
        
        assertEquals("HIGH", analysis.getRiskLevel());
    }
    
    @Test
    void emptyImpactSafe() {
        ImpactAnalysis analysis = service.analyzeImpact("Service.java", null);
        
        assertNotNull(analysis);
        assertEquals("LOW", analysis.getRiskLevel());
    }
}
