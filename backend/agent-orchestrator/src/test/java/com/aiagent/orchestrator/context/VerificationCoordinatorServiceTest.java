package com.aiagent.orchestrator.context;

import com.aiagent.common.model.PatchProposal;
import com.aiagent.orchestrator.context.VerificationCoordinatorService.VerificationResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VerificationCoordinatorServiceTest {
    
    private final VerificationCoordinatorService service = new VerificationCoordinatorService();
    
    @Test
    void configVerification() {
        PatchProposal patch = new PatchProposal();
        patch.setFile("application.yml");
        patch.setSuggestedChange("server:\n  port: 8080");
        
        VerificationResult result = service.verify(List.of(patch));
        
        assertTrue(result.isSuccess());
    }
    
    @Test
    void syntaxVerification() {
        PatchProposal patch = new PatchProposal();
        patch.setFile("UserService.java");
        patch.setSuggestedChange("public class UserService { }");
        
        VerificationResult result = service.verify(List.of(patch));
        
        assertTrue(result.isSuccess());
    }
    
    @Test
    void consistencyVerification() {
        PatchProposal patch = new PatchProposal();
        patch.setFile("UserRepository.java");
        patch.setSuggestedChange("User findById(Long id);");
        
        VerificationResult result = service.verify(List.of(patch));
        
        assertTrue(result.isSuccess());
    }
    
    @Test
    void failureHandling() {
        PatchProposal patch = new PatchProposal();
        patch.setFile("UserService.java");
        patch.setSuggestedChange("public class UserService { ");
        
        VerificationResult result = service.verify(List.of(patch));
        
        assertFalse(result.isSuccess());
        assertFalse(result.getIssues().isEmpty());
    }
}
