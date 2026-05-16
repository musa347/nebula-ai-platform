package com.aiagent.orchestrator.service;

import com.aiagent.common.model.FileBackup;
import com.aiagent.common.model.PatchProposal;
import com.aiagent.common.model.PatchExecutionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SafetyRollbackEngineTest {

    private BackupService backupService;
    private PatchSafetyService patchSafetyService;
    private PatchExecutionService patchExecutionService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        backupService = new BackupService();
        patchSafetyService = new PatchSafetyService();
        patchExecutionService = new PatchExecutionService();
        
        // Use reflection to inject dependencies for testing
        try {
            var backupField = PatchExecutionService.class.getDeclaredField("backupService");
            backupField.setAccessible(true);
            backupField.set(patchExecutionService, backupService);
            
            var safetyField = PatchExecutionService.class.getDeclaredField("patchSafetyService");
            safetyField.setAccessible(true);
            safetyField.set(patchExecutionService, patchSafetyService);
        } catch (Exception e) {
            // Ignore for test
        }
    }

    @Test
    void testBackupCreated() throws IOException {
        // Create test file
        Path testFile = tempDir.resolve("test.java");
        Files.write(testFile, "public class Test {}".getBytes());

        // Create backup
        FileBackup backup = backupService.createBackup(testFile.toString());

        // Verify backup exists
        assertNotNull(backup);
        assertNotNull(backup.getBackupPath());
        assertTrue(Files.exists(Path.of(backup.getBackupPath())));
        assertEquals(testFile.toString(), backup.getFile());
    }

    @Test
    void testSuccessfulPatch() {
        PatchProposal patch = new PatchProposal("UserService.java", "Add caching", "HIGH");
        List<PatchExecutionResult> results = patchExecutionService.executePatches(List.of(patch));

        assertEquals(1, results.size());
        PatchExecutionResult result = results.get(0);
        assertTrue(result.isSuccess());
        assertFalse(result.isReverted());
        assertNotNull(result.getBackupPath());
    }

    @Test
    void testUnsafePatchBlocked() {
        // Test dangerous keyword
        PatchProposal dangerousPatch = new PatchProposal("test.java", "rm -rf /", "HIGH");
        List<PatchExecutionResult> results = patchExecutionService.executePatches(List.of(dangerousPatch));

        assertEquals(1, results.size());
        PatchExecutionResult result = results.get(0);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Unsafe patch blocked"));
        assertTrue(result.getMessage().contains("rm"));
    }

    @Test
    void testSystemFileBlocked() {
        PatchProposal systemPatch = new PatchProposal("/etc/passwd", "Add user", "HIGH");
        List<PatchExecutionResult> results = patchExecutionService.executePatches(List.of(systemPatch));

        assertEquals(1, results.size());
        PatchExecutionResult result = results.get(0);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("System-level file"));
    }

    @Test
    void testEmptyFileRejected() {
        PatchProposal emptyPatch = new PatchProposal("", "Some change", "HIGH");
        List<PatchExecutionResult> results = patchExecutionService.executePatches(List.of(emptyPatch));

        assertEquals(1, results.size());
        PatchExecutionResult result = results.get(0);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("null or blank"));
    }

    @Test
    void testFailureRestore() {
        // Test with file that will fail in simulation
        PatchProposal failingPatch = new PatchProposal("NonExistentFile.java", "Some change", "HIGH");
        List<PatchExecutionResult> results = patchExecutionService.executePatches(List.of(failingPatch));

        assertEquals(1, results.size());
        PatchExecutionResult result = results.get(0);
        assertFalse(result.isSuccess());
        assertTrue(result.isReverted());
        assertNotNull(result.getBackupPath());
        assertTrue(result.getMessage().contains("reverted"));
    }

    @Test
    void testSafetyValidation() {
        PatchSafetyService.SafetyResult safe = patchSafetyService.validate("test.java", "add logging");
        assertTrue(safe.isSafe());

        PatchSafetyService.SafetyResult unsafe = patchSafetyService.validate("test.java", "delete all files");
        assertFalse(unsafe.isSafe());
        assertTrue(unsafe.getReason().contains("delete"));
    }

    @Test
    void testBackupRestore() throws IOException {
        // Create test file with content
        Path testFile = tempDir.resolve("restore-test.java");
        String originalContent = "original content";
        Files.write(testFile, originalContent.getBytes());

        // Create backup
        FileBackup backup = backupService.createBackup(testFile.toString());

        // Modify original file
        Files.write(testFile, "modified content".getBytes());

        // Restore backup
        backupService.restoreBackup(backup);

        // Verify restoration
        String restoredContent = Files.readString(testFile);
        assertEquals(originalContent, restoredContent);
    }
}