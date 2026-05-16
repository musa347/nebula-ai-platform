package com.aiagent.orchestrator.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class PatchSafetyService {
    
    private static final List<String> DANGEROUS_KEYWORDS = Arrays.asList(
        "rm", "delete", "drop", "shutdown", "destroy"
    );
    
    private static final List<String> SYSTEM_PATHS = Arrays.asList(
        "/etc/", "/var/", "system/"
    );
    
    public static class SafetyResult {
        private boolean safe;
        private String reason;
        
        public SafetyResult(boolean safe, String reason) {
            this.safe = safe;
            this.reason = reason;
        }
        
        public boolean isSafe() {
            return safe;
        }
        
        public String getReason() {
            return reason;
        }
    }
    
    public SafetyResult validate(String filePath, String patchContent) {
        // Rule 3: Empty target file
        if (filePath == null || filePath.trim().isEmpty()) {
            return new SafetyResult(false, "File path is null or blank");
        }
        
        // Rule 2: System-level files
        for (String systemPath : SYSTEM_PATHS) {
            if (filePath.contains(systemPath)) {
                return new SafetyResult(false, "System-level file modification blocked: " + systemPath);
            }
        }
        
        // Rule 1: Dangerous keywords
        if (patchContent != null) {
            String lowerContent = patchContent.toLowerCase();
            for (String keyword : DANGEROUS_KEYWORDS) {
                if (lowerContent.contains(keyword)) {
                    return new SafetyResult(false, "Dangerous keyword detected: " + keyword);
                }
            }
        }
        
        return new SafetyResult(true, "Patch is safe");
    }
}