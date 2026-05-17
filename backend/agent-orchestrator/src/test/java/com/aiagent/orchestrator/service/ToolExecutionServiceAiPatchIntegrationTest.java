package com.aiagent.orchestrator.service;

import com.aiagent.common.enums.ToolType;
import com.aiagent.common.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ToolExecutionServiceAiPatchIntegrationTest {

    @Mock
    private ContextLoaderService contextLoaderService;
    
    @Mock
    private FileReaderService fileReaderService;
    
    @Mock
    private PatchProposalService patchProposalService;
    
    @Mock
    private AiPatchGenerationService aiPatchGenerationService;
    
    @Mock
    private PatchExecutionService patchExecutionService;
    
    @Mock
    private ExecutionStatsService executionStatsService;
    
    private ToolExecutionService toolExecutionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        toolExecutionService = new ToolExecutionService();
        
        // Inject dependencies
        ReflectionTestUtils.setField(toolExecutionService, "contextLoaderService", contextLoaderService);
        ReflectionTestUtils.setField(toolExecutionService, "fileReaderService", fileReaderService);
        ReflectionTestUtils.setField(toolExecutionService, "patchProposalService", patchProposalService);
        ReflectionTestUtils.setField(toolExecutionService, "aiPatchGenerationService", aiPatchGenerationService);
        ReflectionTestUtils.setField(toolExecutionService, "patchExecutionService", patchExecutionService);
        ReflectionTestUtils.setField(toolExecutionService, "executionStatsService", executionStatsService);
    }

    @Test
    void testAiPatchGenerationEnabled() {
        // Given
        ReflectionTestUtils.setField(toolExecutionService, "aiPatchEnabled", true);
        
        ToolDecision decision = new ToolDecision(ToolType.PATCH_GENERATE, "AI patch generation");
        String task = "Add caching to UserService";
        String targetFile = "UserService.java";
        
        List<LoadedContext> contexts = List.of(createMockContext("UserService.java"));
        List<FilePreview> previews = List.of(createMockPreview("UserService.java"));
        List<PatchProposal> aiPatches = List.of(createMockAiPatch());
        
        when(contextLoaderService.loadContext(task, targetFile)).thenReturn(contexts);
        when(fileReaderService.readFilePreviews(contexts)).thenReturn(previews);
        when(aiPatchGenerationService.generatePatches(eq(task), anyString(), anyString(), any(Set.class)))
            .thenReturn(aiPatches);

        // When
        ToolExecutionService.ToolExecutionResult result = toolExecutionService.execute(decision, task, targetFile);

        // Then
        assertTrue(result.isSuccess());
        assertEquals("AI patch generation completed", result.getMessage());
        
        @SuppressWarnings("unchecked")
        List<PatchProposal> resultPatches = (List<PatchProposal>) result.getData();
        assertEquals(1, resultPatches.size());
        
        verify(aiPatchGenerationService).generatePatches(eq(task), anyString(), anyString(), any(Set.class));
        verify(executionStatsService).recordSuccess(eq(ToolType.PATCH_GENERATE), anyLong());
        verifyNoInteractions(patchProposalService);
    }

    @Test
    void testAiPatchGenerationDisabled() {
        // Given
        ReflectionTestUtils.setField(toolExecutionService, "aiPatchEnabled", false);
        
        ToolDecision decision = new ToolDecision(ToolType.PATCH_GENERATE, "Rule-based patch generation");
        String task = "Add logging to UserService";
        String targetFile = "UserService.java";
        
        List<LoadedContext> contexts = List.of(createMockContext("UserService.java"));
        List<FilePreview> previews = List.of(createMockPreview("UserService.java"));
        List<PatchProposal> ruleBasedPatches = List.of(createMockRuleBasedPatch());
        
        when(contextLoaderService.loadContext(task, targetFile)).thenReturn(contexts);
        when(fileReaderService.readFilePreviews(contexts)).thenReturn(previews);
        when(patchProposalService.generatePatches(task, contexts, previews)).thenReturn(ruleBasedPatches);

        // When
        ToolExecutionService.ToolExecutionResult result = toolExecutionService.execute(decision, task, targetFile);

        // Then
        assertTrue(result.isSuccess());
        assertEquals("Rule-based patch generation completed", result.getMessage());
        
        @SuppressWarnings("unchecked")
        List<PatchProposal> resultPatches = (List<PatchProposal>) result.getData();
        assertEquals(1, resultPatches.size());
        
        verify(patchProposalService).generatePatches(task, contexts, previews);
        verifyNoInteractions(aiPatchGenerationService);
    }

    private LoadedContext createMockContext(String file) {
        LoadedContext context = new LoadedContext();
        context.setFile(file);
        context.setReason("SERVICE");
        return context;
    }
    
    private FilePreview createMockPreview(String file) {
        FilePreview preview = new FilePreview();
        preview.setFile(file);
        preview.setPreview("public class " + file.replace(".java", "") + " { public void process() { } }");
        return preview;
    }
    
    private PatchProposal createMockAiPatch() {
        PatchProposal patch = new PatchProposal();
        patch.setFile("UserService.java");
        patch.setDescription("AI-generated patch");
        patch.setSuggestedChange("@Cacheable(\"users\")\npublic User findById(Long id) { }");
        return patch;
    }
    
    private PatchProposal createMockRuleBasedPatch() {
        PatchProposal patch = new PatchProposal();
        patch.setFile("UserService.java");
        patch.setDescription("Rule-based patch");
        patch.setSuggestedChange("public void newMethod() { }");
        return patch;
    }
}