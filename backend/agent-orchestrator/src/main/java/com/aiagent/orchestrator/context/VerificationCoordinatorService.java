package com.aiagent.orchestrator.context;

import com.aiagent.common.model.PatchProposal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class VerificationCoordinatorService {
    private static final Logger log = LoggerFactory.getLogger(VerificationCoordinatorService.class);
    
    public VerificationResult verify(List<PatchProposal> patches) {
        List<String> issues = new ArrayList<>();
        
        for (PatchProposal patch : patches) {
            String file = patch.getFile();
            
            // Config verification
            if (file.endsWith(".yml") || file.endsWith(".yaml") || file.endsWith(".properties")) {
                if (!verifyConfigStructure(patch)) {
                    issues.add("Invalid config structure in " + file);
                }
            }
            
            // Java syntax verification
            if (file.endsWith(".java")) {
                if (!verifySyntax(patch)) {
                    issues.add("Syntax error in " + file);
                }
            }
            
            // Interface consistency
            if (file.contains("Interface") || file.contains("Repository")) {
                if (!verifyConsistency(patch)) {
                    issues.add("Consistency issue in " + file);
                }
            }
        }
        
        boolean success = issues.isEmpty();
        log.info("Verification result: {} (issues: {})", success ? "PASS" : "FAIL", issues.size());
        return new VerificationResult(success, issues);
    }
    
    private boolean verifyConfigStructure(PatchProposal patch) {
        String content = patch.getSuggestedChange();
        if (content == null || content.isBlank()) {
            return false;
        }
        // Lightweight check: no obvious syntax errors
        return !content.contains("::") && !content.contains("}{");
    }
    
    private boolean verifySyntax(PatchProposal patch) {
        String content = patch.getSuggestedChange();
        if (content == null || content.isBlank()) {
            return false;
        }
        // Lightweight check: balanced braces
        long openBraces = content.chars().filter(ch -> ch == '{').count();
        long closeBraces = content.chars().filter(ch -> ch == '}').count();
        return openBraces == closeBraces;
    }
    
    private boolean verifyConsistency(PatchProposal patch) {
        String content = patch.getSuggestedChange();
        if (content == null || content.isBlank()) {
            return false;
        }
        // Lightweight check: has method signatures
        return content.contains("(") && content.contains(")");
    }
    
    public static class VerificationResult {
        private final boolean success;
        private final List<String> issues;
        
        public VerificationResult(boolean success, List<String> issues) {
            this.success = success;
            this.issues = issues;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public List<String> getIssues() {
            return issues;
        }
    }
}
