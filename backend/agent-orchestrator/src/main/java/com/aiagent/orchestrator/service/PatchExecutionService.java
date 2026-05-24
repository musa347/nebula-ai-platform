package com.aiagent.orchestrator.service;

import com.aiagent.common.model.PatchProposal;
import com.aiagent.common.model.PatchExecutionResult;
import com.aiagent.common.model.FileBackup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.*;

@Service
public class PatchExecutionService {

    private static final Logger log = LoggerFactory.getLogger(PatchExecutionService.class);

    @Value("${mcp.server.url:http://localhost:8081}")
    private String mcpServerUrl;

    @Autowired
    private BackupService backupService;

    @Autowired
    private PatchSafetyService patchSafetyService;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<PatchExecutionResult> executePatches(List<PatchProposal> patches) {
        return executePatches(patches, null);
    }

    public List<PatchExecutionResult> executePatches(List<PatchProposal> patches, String workspacePath) {
        List<PatchExecutionResult> results = new ArrayList<>();

        if (patches == null || patches.isEmpty()) {
            return results;
        }

        for (PatchProposal patch : patches) {
            try {
                PatchExecutionResult result = executePatch(patch, workspacePath);
                results.add(result);
            } catch (Exception e) {
                log.warn("Failed to execute patch for file: {}", patch.getFile(), e);
                results.add(new PatchExecutionResult(
                        patch.getFile(),
                        false,
                        "Patch execution failed: " + e.getMessage()
                ));
            }
        }

        log.info("Executed {} patches with {} successes",
                patches.size(),
                results.stream().mapToInt(r -> r.isSuccess() ? 1 : 0).sum());

        return results;
    }

    private PatchExecutionResult executePatch(PatchProposal patch, String workspacePath) {
        FileBackup backup = null;

        try {
            PatchSafetyService.SafetyResult safetyResult = patchSafetyService.validate(
                    patch.getFile(),
                    patch.getDescription()
            );

            if (!safetyResult.isSafe()) {
                PatchExecutionResult result = new PatchExecutionResult(
                        patch.getFile(),
                        false,
                        "Unsafe patch blocked: " + safetyResult.getReason()
                );
                return result;
            }

            backup = backupService.createBackup(patch.getFile());

            PatchExecutionResult result = simulatePatchExecution(patch, workspacePath);
            result.setBackupPath(backup.getBackupPath());

            if (!result.isSuccess()) {
                backupService.restoreBackup(backup);
                result.setReverted(true);
                result.setMessage(result.getMessage() + " (reverted from backup)");
            }

            return result;

        } catch (Exception e) {
            log.warn("Failed to execute patch for file: {}", patch.getFile(), e);

            if (backup != null) {
                try {
                    backupService.restoreBackup(backup);
                } catch (Exception restoreError) {
                    log.error("Failed to restore backup: {}", backup.getBackupPath(), restoreError);
                }
            }

            PatchExecutionResult result = new PatchExecutionResult(
                    patch.getFile(),
                    false,
                    "Patch execution error: " + e.getMessage()
            );
            result.setReverted(backup != null);
            if (backup != null) {
                result.setBackupPath(backup.getBackupPath());
            }
            return result;
        }
    }

    private PatchExecutionResult simulatePatchExecution(PatchProposal patch, String workspacePath) {
        String file = patch.getFile();
        String suggestedChange = patch.getSuggestedChange();

        if (suggestedChange == null || suggestedChange.trim().isEmpty()) {
            return new PatchExecutionResult(file, false, "Empty patch content");
        }

        String trimmed = suggestedChange.trim();
        if (trimmed.startsWith("//") || trimmed.startsWith("/*")) {
            return new PatchExecutionResult(file, false, "Invalid patch: contains only comments, not modified code");
        }

        String absolutePath = file;
        if (workspacePath != null && !file.startsWith("/")) {
            absolutePath = workspacePath + "/" + file;
            log.info("Converted relative path '{}' to absolute: '{}'", file, absolutePath);
        }

        try {
            String endpoint = mcpServerUrl + "/api/tools/execute";

            Map<String, Object> request = new HashMap<>();
            request.put("toolName", "filesystem.write");
            request.put("toolType", "FILESYSTEM_WRITE");

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("path", absolutePath);
            parameters.put("content", suggestedChange);
            request.put("parameters", parameters);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            log.info("Writing file via MCP: {}", absolutePath);
            Map<String, Object> response = restTemplate.postForObject(endpoint, entity, Map.class);

            if (response != null && "SUCCESS".equals(response.get("status"))) {
                return new PatchExecutionResult(file, true, "Successfully applied " + patch.getDescription());
            } else {
                String error = response != null ? String.valueOf(response.get("errorMessage")) : "Unknown error";
                return new PatchExecutionResult(file, false, "MCP write failed: " + error);
            }

        } catch (Exception e) {
            log.error("Failed to write file via MCP: {}", absolutePath, e);
            return new PatchExecutionResult(file, false, "Write error: " + e.getMessage());
        }
    }
}