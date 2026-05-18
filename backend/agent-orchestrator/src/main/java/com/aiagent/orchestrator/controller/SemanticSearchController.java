package com.aiagent.orchestrator.controller;

import com.aiagent.orchestrator.embedding.EmbeddingQueryService;
import com.aiagent.orchestrator.embedding.SemanticMatch;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/repo")
public class SemanticSearchController {
    
    private final EmbeddingQueryService queryService;
    
    public SemanticSearchController(EmbeddingQueryService queryService) {
        this.queryService = queryService;
    }
    
    @PostMapping("/semantic-search")
    public Map<String, List<SemanticMatch>> search(@RequestBody Map<String, String> request) {
        String query = request.getOrDefault("query", "");
        int k = Integer.parseInt(request.getOrDefault("k", "5"));
        
        List<SemanticMatch> matches = queryService.query(query, k);
        return Map.of("matches", matches);
    }
}
