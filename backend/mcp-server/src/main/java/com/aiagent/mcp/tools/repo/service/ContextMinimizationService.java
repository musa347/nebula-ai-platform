package com.aiagent.mcp.tools.repo.service;

import com.aiagent.common.dto.*;
import com.aiagent.common.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ContextMinimizationService {

    private static final Logger log = LoggerFactory.getLogger(ContextMinimizationService.class);
    
    private final RepoSearchService repoSearchService;
    private final RepoGrepService repoGrepService;
    private final SymbolSearchService symbolSearchService;
    private final DependencyMapService dependencyMapService;
    private final JavaParserService javaParserService;

    public ContextMinimizationService(RepoSearchService repoSearchService,
                                     RepoGrepService repoGrepService,
                                     SymbolSearchService symbolSearchService,
                                     DependencyMapService dependencyMapService,
                                     JavaParserService javaParserService) {
        this.repoSearchService = repoSearchService;
        this.repoGrepService = repoGrepService;
        this.symbolSearchService = symbolSearchService;
        this.dependencyMapService = dependencyMapService;
        this.javaParserService = javaParserService;
    }

    public ContextResponse buildContext(ContextRequest request, String workspacePath) {
        String task = request.getTask();
        List<ContextSlice> slices = new ArrayList<>();
        Set<String> includedFiles = new HashSet<>();

        // Extract keywords from task
        List<String> keywords = extractKeywords(task);
        
        // Step 1: Find relevant files via symbol search
        for (String keyword : keywords) {
            SymbolSearchResponse symbolResponse = symbolSearchService.search(
                new SymbolSearchRequest(keyword), workspacePath);
            
            for (SymbolMatch match : symbolResponse.getMatches()) {
                if (!includedFiles.contains(match.getFile())) {
                    addFileSlice(match.getFile(), "Primary match for: " + keyword, 
                               slices, includedFiles, workspacePath);
                }
            }
        }

        // Step 2: Find files via grep (content search)
        for (String keyword : keywords) {
            RepoGrepResponse grepResponse = repoGrepService.grep(
                new RepoGrepRequest(keyword), workspacePath);
            
            List<String> grepFiles = grepResponse.getMatches().stream()
                .map(SearchMatch::getFile)
                .distinct()
                .limit(3) // Limit grep results
                .toList();
            
            for (String file : grepFiles) {
                if (!includedFiles.contains(file)) {
                    addFileSlice(file, "Content match for: " + keyword, 
                               slices, includedFiles, workspacePath);
                }
            }
        }

        // Step 3: Add dependency-related classes
        List<String> filesToCheck = new ArrayList<>(includedFiles);
        for (String file : filesToCheck) {
            Path filePath = Paths.get(file);
            List<DependencyEdge> deps = dependencyMapService.extractDependencies(filePath);
            
            for (DependencyEdge dep : deps) {
                // Find files containing the dependency
                SymbolSearchResponse depSearch = symbolSearchService.search(
                    new SymbolSearchRequest(dep.getToClass()), workspacePath);
                
                for (SymbolMatch match : depSearch.getMatches()) {
                    if (!includedFiles.contains(match.getFile()) && includedFiles.size() < 10) {
                        addFileSlice(match.getFile(), 
                                   "Dependency of " + dep.getFromClass() + " (" + dep.getType() + ")",
                                   slices, includedFiles, workspacePath);
                    }
                }
            }
        }

        log.info("Built context with {} slices for task: {}", slices.size(), task);
        return new ContextResponse(slices);
    }

    private void addFileSlice(String file, String reason, List<ContextSlice> slices, 
                             Set<String> includedFiles, String workspacePath) {
        try {
            Path filePath = Paths.get(file);
            if (!filePath.isAbsolute()) {
                filePath = Paths.get(workspacePath, file);
            }
            
            if (!Files.exists(filePath)) {
                return;
            }

            // Extract only relevant methods/classes
            ClassMetadata metadata = javaParserService.extractMetadata(filePath);
            String content;
            
            if (metadata != null && !metadata.getMethods().isEmpty()) {
                // Include class signature + methods (minimal)
                content = buildMinimalContent(filePath, metadata);
            } else {
                // Fallback to full file if parsing fails
                content = Files.readString(filePath);
            }

            slices.add(new ContextSlice(file, content, reason));
            includedFiles.add(file);
            
        } catch (IOException e) {
            log.warn("Could not read file: {}", file, e);
        }
    }

    private String buildMinimalContent(Path filePath, ClassMetadata metadata) throws IOException {
        String fullContent = Files.readString(filePath);
        
        // For now, return full content
        // TODO: Extract only class signature + method signatures
        return fullContent;
    }

    private List<String> extractKeywords(String task) {
        // Simple keyword extraction: split on spaces, filter common words
        Set<String> stopWords = Set.of("add", "to", "the", "a", "an", "in", "on", "for", "with");
        
        return Arrays.stream(task.split("\\s+"))
            .map(String::toLowerCase)
            .filter(word -> word.length() > 2)
            .filter(word -> !stopWords.contains(word))
            .collect(Collectors.toList());
    }
}
