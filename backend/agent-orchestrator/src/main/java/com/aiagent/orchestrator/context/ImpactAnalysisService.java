package com.aiagent.orchestrator.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ImpactAnalysisService {
    private static final Logger log = LoggerFactory.getLogger(ImpactAnalysisService.class);
    
    public ImpactAnalysis analyzeImpact(String primaryFile, List<RelatedContext> relatedFiles) {
        List<String> affectedFiles = new ArrayList<>();
        List<String> affectedClasses = new ArrayList<>();
        
        affectedFiles.add(primaryFile);
        affectedClasses.add(extractClassName(primaryFile));
        
        if (relatedFiles != null) {
            for (RelatedContext related : relatedFiles) {
                affectedFiles.add(related.getFile());
                affectedClasses.add(extractClassName(related.getFile()));
            }
        }
        
        String riskLevel = calculateRiskLevel(primaryFile, affectedFiles.size());
        
        log.info("Impact analysis: {} affected files, risk={}", affectedFiles.size(), riskLevel);
        return new ImpactAnalysis(affectedFiles, affectedClasses, riskLevel);
    }
    
    private String calculateRiskLevel(String primaryFile, int affectedFileCount) {
        // HIGH: interface/config/shared component
        if (primaryFile.contains("Interface") || primaryFile.contains("Config") || 
            primaryFile.contains("Shared") || primaryFile.contains("Common")) {
            return "HIGH";
        }
        
        // MEDIUM: 2-3 files
        if (affectedFileCount >= 2 && affectedFileCount <= 3) {
            return "MEDIUM";
        }
        
        // LOW: single file
        if (affectedFileCount == 1) {
            return "LOW";
        }
        
        // HIGH: more than 3 files
        return "HIGH";
    }
    
    private String extractClassName(String file) {
        if (file.contains("/")) {
            file = file.substring(file.lastIndexOf('/') + 1);
        }
        if (file.contains(".")) {
            file = file.substring(0, file.lastIndexOf('.'));
        }
        return file;
    }
}
