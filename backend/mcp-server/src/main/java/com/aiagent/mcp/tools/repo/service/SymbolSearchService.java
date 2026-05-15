package com.aiagent.mcp.tools.repo.service;

import com.aiagent.common.dto.SymbolSearchRequest;
import com.aiagent.common.dto.SymbolSearchResponse;
import com.aiagent.common.model.ClassMetadata;
import com.aiagent.common.model.SymbolMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class SymbolSearchService {

    private static final Logger log = LoggerFactory.getLogger(SymbolSearchService.class);
    private static final List<String> IGNORED_DIRS = List.of(".git", "target", "build", "node_modules", ".idea");
    
    private final JavaParserService javaParserService;

    public SymbolSearchService(JavaParserService javaParserService) {
        this.javaParserService = javaParserService;
    }

    public SymbolSearchResponse search(SymbolSearchRequest request, String workspacePath) {
        String query = request.getQuery().toLowerCase();
        Path root = Paths.get(workspacePath);
        List<SymbolMatch> matches = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile)
                 .filter(path -> path.toString().endsWith(".java"))
                 .filter(path -> !isIgnored(path))
                 .forEach(path -> searchInFile(path, query, matches));

            log.info("Found {} symbol matches for query: {}", matches.size(), request.getQuery());
            return new SymbolSearchResponse(matches);
        } catch (IOException e) {
            log.error("Error searching symbols", e);
            return new SymbolSearchResponse(List.of());
        }
    }

    private void searchInFile(Path path, String query, List<SymbolMatch> matches) {
        ClassMetadata metadata = javaParserService.extractMetadata(path);
        if (metadata == null) {
            return;
        }

        String className = metadata.getClassName();
        String filePath = path.toString();

        // Match class name
        if (className.toLowerCase().contains(query)) {
            matches.add(new SymbolMatch(className, null, filePath));
        }

        // Match method names
        for (String method : metadata.getMethods()) {
            if (method.toLowerCase().contains(query)) {
                matches.add(new SymbolMatch(className, method, filePath));
            }
        }
    }

    private boolean isIgnored(Path path) {
        return IGNORED_DIRS.stream()
                .anyMatch(ignored -> path.toString().contains("/" + ignored + "/") || 
                                    path.toString().contains("\\" + ignored + "\\"));
    }
}
