package com.aiagent.mcp.tools.repo.controller;

import com.aiagent.common.dto.*;
import com.aiagent.mcp.tools.repo.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/repo")
public class RepoController {

    private static final Logger log = LoggerFactory.getLogger(RepoController.class);
    private final RepoSearchService repoSearchService;
    private final RepoGrepService repoGrepService;
    private final SymbolSearchService symbolSearchService;
    private final ContextMinimizationService contextMinimizationService;

    @Value("${mcp.workspace.path:./}")
    private String workspacePath;

    public RepoController(RepoSearchService repoSearchService, RepoGrepService repoGrepService, 
                         SymbolSearchService symbolSearchService, ContextMinimizationService contextMinimizationService) {
        this.repoSearchService = repoSearchService;
        this.repoGrepService = repoGrepService;
        this.symbolSearchService = symbolSearchService;
        this.contextMinimizationService = contextMinimizationService;
    }

    @PostMapping("/search")
    public ResponseEntity<RepoSearchResponse> search(@RequestBody RepoSearchRequest request) {
        log.info("Searching repository for: {}", request.getQuery());
        RepoSearchResponse response = repoSearchService.search(request, workspacePath);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/grep")
    public ResponseEntity<RepoGrepResponse> grep(@RequestBody RepoGrepRequest request) {
        log.info("Grepping repository for: {}", request.getQuery());
        RepoGrepResponse response = repoGrepService.grep(request, workspacePath);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/symbol/search")
    public ResponseEntity<SymbolSearchResponse> symbolSearch(@RequestBody SymbolSearchRequest request) {
        log.info("Searching symbols for: {}", request.getQuery());
        SymbolSearchResponse response = symbolSearchService.search(request, workspacePath);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/context")
    public ResponseEntity<ContextResponse> buildContext(@RequestBody ContextRequest request) {
        log.info("Building context for task: {}", request.getTask());
        ContextResponse response = contextMinimizationService.buildContext(request, workspacePath);
        return ResponseEntity.ok(response);
    }
}
