package com.aiagent.orchestrator.service;

import com.aiagent.common.model.PatchProposal;
import com.aiagent.common.model.LoadedContext;
import com.aiagent.common.model.FilePreview;
import com.aiagent.orchestrator.dto.AiPatchRequest;
import com.aiagent.orchestrator.dto.AiPatchResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Service for generating patch proposals using AI with comprehensive safety validation.
 * Implements fallback to rule-based patch engine when AI fails or produces unsafe results.
 */
@Service
public class AiPatchGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(AiPatchGenerationService.class);

    // System paths that should be rejected for security
    private static final Set<String> FORBIDDEN_PATHS = Set.of(
        "/etc", "/system", "/usr/bin", "/bin", "/sbin", "/boot", "/dev", "/proc", "/sys",
        "C:\\Windows", "C:\\System32", "/Library/System", "/System/Library"
    );

    private final AiPatchPromptService promptService;
    private final AiReasoningService aiReasoningService;
    private final PatchProposalService fallbackPatchService;
    private final ObjectMapper objectMapper;

    public AiPatchGenerationService(
            AiPatchPromptService promptService,
            AiReasoningService aiReasoningService,
            PatchProposalService fallbackPatchService,
            ObjectMapper objectMapper) {
        this.promptService = promptService;
        this.aiReasoningService = aiReasoningService;
        this.fallbackPatchService = fallbackPatchService;
        this.objectMapper = objectMapper;
    }

    /**
     * Generates patch proposals using AI with safety validation and fallback.
     *
     * @param task The task description
     * @param context Additional context information
     * @param filePreview Current file content
     * @param knownContextFiles Set of known valid context files for validation
     * @return List of validated patch proposals
     */
    public List<PatchProposal> generatePatches(String task, String context, String filePreview, Set<String> knownContextFiles) {
        if (task == null || task.trim().isEmpty()) {
            logger.warn("Empty task provided, using fallback patch engine");
            return fallbackToRuleBasedEngine(task, context, filePreview);
        }

        try {
            // Build AI request
            AiPatchRequest request = new AiPatchRequest(task, extractFileName(filePreview), context, filePreview);
            
            // Generate prompt
            String prompt = promptService.buildPatchPrompt(request);
            logger.debug("Generated AI prompt for task: {}", task);
            
            // Call AI service
            String aiResponse = aiReasoningService.ask(prompt);
            logger.debug("Received AI response for task: {}", task);
            
            // Parse and validate response
            AiPatchResponse patchResponse = parseAndValidateResponse(aiResponse, knownContextFiles);
            
            if (patchResponse != null) {
                // Convert to PatchProposal
                PatchProposal proposal = convertToPatchProposal(patchResponse, task);
                logger.info("Successfully generated AI patch proposal for task: {}", task);
                return List.of(proposal);
            }
            
        } catch (Exception e) {
            logger.warn("AI patch generation failed for task: {}, falling back to rule-based engine. Error: {}", 
                       task, e.getMessage());
        }
        
        // Fallback to rule-based engine
        return fallbackToRuleBasedEngine(task, context, filePreview);
    }

    /**
     * Generates patch proposals for multiple files.
     *
     * @param task The task description
     * @param contextFiles Map of file names to their content
     * @return List of validated patch proposals
     */
    public List<PatchProposal> generatePatchesForFiles(String task, java.util.Map<String, String> contextFiles) {
        List<PatchProposal> allProposals = new ArrayList<>();
        
        for (java.util.Map.Entry<String, String> entry : contextFiles.entrySet()) {
            String fileName = entry.getKey();
            String fileContent = entry.getValue();
            
            List<PatchProposal> fileProposals = generatePatches(
                task, 
                "Processing file: " + fileName, 
                fileContent, 
                contextFiles.keySet()
            );
            
            allProposals.addAll(fileProposals);
        }
        
        return allProposals;
    }

    /**
     * Parses AI response and validates structure and safety.
     *
     * @param aiResponse Raw AI response string
     * @param knownContextFiles Set of known valid files for validation
     * @return Validated AiPatchResponse or null if invalid
     */
    private AiPatchResponse parseAndValidateResponse(String aiResponse, Set<String> knownContextFiles) {
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            logger.warn("Empty AI response received");
            return null;
        }

        try {
            // Parse JSON
            AiPatchResponse response = objectMapper.readValue(aiResponse, AiPatchResponse.class);
            
            // Validate structure
            if (!isValidStructure(response)) {
                logger.warn("Invalid AI response structure");
                return null;
            }
            
            // Validate safety
            if (!isSafeResponse(response, knownContextFiles)) {
                logger.warn("Unsafe AI response detected, rejecting");
                return null;
            }
            
            return response;
            
        } catch (Exception e) {
            logger.warn("Failed to parse AI response as JSON: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Validates the structure of AI response.
     *
     * @param response The parsed AI response
     * @return true if structure is valid
     */
    private boolean isValidStructure(AiPatchResponse response) {
        if (response == null) {
            return false;
        }
        
        // Check required fields
        if (response.getFile() == null || response.getFile().trim().isEmpty()) {
            logger.warn("Missing file in AI response");
            return false;
        }
        
        if (response.getDescription() == null || response.getDescription().trim().isEmpty()) {
            logger.warn("Missing description in AI response");
            return false;
        }
        
        if (response.getSuggestedChange() == null || response.getSuggestedChange().trim().isEmpty()) {
            logger.warn("Missing suggestedChange in AI response");
            return false;
        }
        
        return true;
    }

    /**
     * Validates safety of AI response according to security rules.
     *
     * @param response The AI response to validate
     * @param knownContextFiles Set of known valid files
     * @return true if response is safe
     */
    private boolean isSafeResponse(AiPatchResponse response, Set<String> knownContextFiles) {
        String filePath = response.getFile();
        
        // Check for system file paths
        if (isSystemPath(filePath)) {
            logger.warn("Rejected system file path: {}", filePath);
            return false;
        }
        
        // Check against known context files if provided
        if (knownContextFiles != null && !knownContextFiles.isEmpty()) {
            boolean isKnownFile = knownContextFiles.stream()
                .anyMatch(knownFile -> knownFile.equals(filePath) || knownFile.endsWith("/" + filePath));
            
            if (!isKnownFile) {
                logger.warn("File not in known context: {}", filePath);
                return false;
            }
        }
        
        // Check for suspicious content in suggested changes
        String suggestedChange = response.getSuggestedChange();
        if (containsSuspiciousContent(suggestedChange)) {
            logger.warn("Suspicious content detected in suggested change");
            return false;
        }
        
        return true;
    }

    /**
     * Checks if file path is a system path that should be forbidden.
     *
     * @param filePath The file path to check
     * @return true if it's a system path
     */
    private boolean isSystemPath(String filePath) {
        if (filePath == null) {
            return false;
        }
        
        String normalizedPath = filePath.toLowerCase();
        return FORBIDDEN_PATHS.stream()
            .anyMatch(forbiddenPath -> normalizedPath.startsWith(forbiddenPath.toLowerCase()));
    }

    /**
     * Checks for suspicious content in suggested changes.
     *
     * @param suggestedChange The suggested change content
     * @return true if suspicious content is detected
     */
    private boolean containsSuspiciousContent(String suggestedChange) {
        if (suggestedChange == null) {
            return false;
        }
        
        String lowerContent = suggestedChange.toLowerCase();
        
        // Check for dangerous operations
        String[] suspiciousPatterns = {
            "rm -rf", "sudo", "chmod 777", "exec(", "eval(", 
            "system(", "runtime.exec", "processbuilder", 
            "delete from", "drop table", "truncate"
        };
        
        for (String pattern : suspiciousPatterns) {
            if (lowerContent.contains(pattern)) {
                logger.warn("Detected suspicious pattern: {}", pattern);
                return true;
            }
        }
        
        return false;
    }

    /**
     * Converts AI response to PatchProposal.
     *
     * @param response The validated AI response
     * @param originalTask The original task description
     * @return PatchProposal object
     */
    private PatchProposal convertToPatchProposal(AiPatchResponse response, String originalTask) {
        PatchProposal proposal = new PatchProposal();
        proposal.setFile(response.getFile());
        proposal.setDescription(response.getDescription());
        proposal.setSuggestedChange(response.getSuggestedChange());
        return proposal;
    }

    /**
     * Extracts file name from file preview content.
     *
     * @param filePreview The file content
     * @return Extracted file name or default
     */
    private String extractFileName(String filePreview) {
        if (filePreview == null || filePreview.trim().isEmpty()) {
            return "unknown.java";
        }
        
        // Try to extract class name from Java content
        String[] lines = filePreview.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("public class ") || line.startsWith("class ")) {
                String[] parts = line.split("\\s+");
                for (int i = 0; i < parts.length - 1; i++) {
                    if ("class".equals(parts[i])) {
                        String className = parts[i + 1].replaceAll("[^a-zA-Z0-9]", "");
                        return className + ".java";
                    }
                }
            }
        }
        
        return "extracted.java";
    }

    /**
     * Fallback to rule-based patch engine when AI fails.
     *
     * @param task The task description
     * @param context Additional context
     * @param filePreview File content
     * @return List of patch proposals from rule-based engine
     */
    private List<PatchProposal> fallbackToRuleBasedEngine(String task, String context, String filePreview) {
        logger.info("Using fallback rule-based patch engine for task: {}", task);
        
        try {
            // Create mock contexts and previews for the fallback service
            List<LoadedContext> contexts = List.of(new LoadedContext(extractFileName(filePreview), "AI_FALLBACK"));
            List<FilePreview> previews = List.of(new FilePreview(extractFileName(filePreview), filePreview));
            
            // Use existing PatchProposalService as fallback
            return fallbackPatchService.generatePatches(task, contexts, previews);
        } catch (Exception e) {
            logger.error("Fallback patch engine also failed: {}", e.getMessage());
            return List.of(); // Return empty list as last resort
        }
    }
}