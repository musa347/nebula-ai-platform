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


    public List<PatchProposal> generatePatches(String task, String context, String filePreview, Set<String> knownContextFiles) {
        if (task == null || task.trim().isEmpty()) {
            logger.warn("Empty task provided, using fallback patch engine");
            return fallbackToRuleBasedEngine(task, context, filePreview);
        }

        try {

            AiPatchRequest request = new AiPatchRequest(task, extractFileName(filePreview), context, filePreview);

            String prompt = promptService.buildPatchPrompt(request);
            logger.debug("Generated AI prompt for task: {}", task);

            String aiResponse = aiReasoningService.ask(prompt);
            logger.info("[AI PATCH] Raw AI response: {}", aiResponse);

            AiPatchResponse patchResponse = parseAndValidateResponse(aiResponse, knownContextFiles);

            if (patchResponse != null) {
                PatchProposal proposal = convertToPatchProposal(patchResponse, task);
                logger.info("[AI PATCH] Generated patch - File: {}, Description: {}, Change length: {}",
                        proposal.getFile(), proposal.getDescription(), proposal.getSuggestedChange().length());
                logger.info("[AI PATCH] Suggested change content:\n{}", proposal.getSuggestedChange());
                return List.of(proposal);
            }

        } catch (Exception e) {
            logger.warn("AI patch generation failed for task: {}, falling back to rule-based engine. Error: {}",
                    task, e.getMessage());
        }
        return fallbackToRuleBasedEngine(task, context, filePreview);
    }

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

    private AiPatchResponse parseAndValidateResponse(String aiResponse, Set<String> knownContextFiles) {
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            logger.warn("Empty AI response received");
            return null;
        }

        try {
            String cleanedResponse = stripMarkdownCodeBlocks(aiResponse);
            AiPatchResponse response = objectMapper.readValue(cleanedResponse, AiPatchResponse.class);

            if (!isValidStructure(response)) {
                logger.warn("Invalid AI response structure");
                return null;
            }

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

    private String stripMarkdownCodeBlocks(String response) {
        if (response == null) {
            return null;
        }

        String cleaned = response.trim();

        if (cleaned.startsWith("```")) {
            int firstNewline = cleaned.indexOf('\n');
            if (firstNewline > 0) {
                cleaned = cleaned.substring(firstNewline + 1);
            }
        }

        if (cleaned.endsWith("```")) {
            int lastCodeBlock = cleaned.lastIndexOf("```");
            if (lastCodeBlock > 0) {
                cleaned = cleaned.substring(0, lastCodeBlock);
            }
        }

        return cleaned.trim();
    }

    private boolean isValidStructure(AiPatchResponse response) {
        if (response == null) {
            return false;
        }

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

    private boolean isSafeResponse(AiPatchResponse response, Set<String> knownContextFiles) {
        String filePath = response.getFile();

        if (isSystemPath(filePath)) {
            logger.warn("Rejected system file path: {}", filePath);
            return false;
        }

        if (knownContextFiles != null && !knownContextFiles.isEmpty()) {
            boolean isKnownFile = knownContextFiles.stream()
                    .anyMatch(knownFile -> knownFile.equals(filePath) || knownFile.endsWith("/" + filePath));

            if (!isKnownFile) {
                logger.warn("File not in known context: {}", filePath);
                return false;
            }
        }

        String suggestedChange = response.getSuggestedChange();
        if (containsSuspiciousContent(suggestedChange)) {
            logger.warn("Suspicious content detected in suggested change");
            return false;
        }

        return true;
    }

    private boolean isSystemPath(String filePath) {
        if (filePath == null) {
            return false;
        }

        String normalizedPath = filePath.toLowerCase();
        return FORBIDDEN_PATHS.stream()
                .anyMatch(forbiddenPath -> normalizedPath.startsWith(forbiddenPath.toLowerCase()));
    }


    private boolean containsSuspiciousContent(String suggestedChange) {
        if (suggestedChange == null) {
            return false;
        }

        String lowerContent = suggestedChange.toLowerCase();

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

    private PatchProposal convertToPatchProposal(AiPatchResponse response, String originalTask) {
        PatchProposal proposal = new PatchProposal();
        proposal.setFile(response.getFile());
        proposal.setDescription(response.getDescription());
        proposal.setSuggestedChange(response.getSuggestedChange());
        return proposal;
    }

    private String extractFileName(String filePreview) {
        if (filePreview == null || filePreview.trim().isEmpty()) {
            return "unknown.java";
        }
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

    private List<PatchProposal> fallbackToRuleBasedEngine(String task, String context, String filePreview) {
        logger.info("Using fallback rule-based patch engine for task: {}", task);

        try {
            List<LoadedContext> contexts = List.of(new LoadedContext(extractFileName(filePreview), "AI_FALLBACK"));
            List<FilePreview> previews = List.of(new FilePreview(extractFileName(filePreview), filePreview));

            return fallbackPatchService.generatePatches(task, contexts, previews);
        } catch (Exception e) {
            logger.error("Fallback patch engine also failed: {}", e.getMessage());
            return List.of();
        }
    }
}