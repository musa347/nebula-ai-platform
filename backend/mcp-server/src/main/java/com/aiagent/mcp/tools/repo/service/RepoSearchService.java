package com.aiagent.mcp.tools.repo.service;

import com.aiagent.common.dto.RepoSearchRequest;
import com.aiagent.common.dto.RepoSearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

@Service
public class RepoSearchService {

    private static final Logger log = LoggerFactory.getLogger(RepoSearchService.class);
    private static final List<String> IGNORED_DIRS = List.of(".git", "target", "build", "node_modules", ".idea");

    public RepoSearchResponse search(RepoSearchRequest request, String workspacePath) {
        String query = request.getQuery().toLowerCase();
        Path root = Paths.get(workspacePath);

        try (Stream<Path> paths = Files.walk(root)) {
            List<String> matches = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> !isIgnored(path))
                    .filter(path -> matchesQuery(path, query))
                    .map(Path::toString)
                    .toList();

            log.info("Found {} files matching query: {}", matches.size(), request.getQuery());
            return new RepoSearchResponse(matches);
        } catch (IOException e) {
            log.error("Error searching repository", e);
            return new RepoSearchResponse(List.of());
        }
    }

    private boolean isIgnored(Path path) {
        return IGNORED_DIRS.stream()
                .anyMatch(ignored -> path.toString().contains("/" + ignored + "/") || 
                                    path.toString().contains("\\" + ignored + "\\"));
    }

    private boolean matchesQuery(Path path, String query) {
        String fileName = path.getFileName().toString().toLowerCase();
        return fileName.contains(query);
    }
}
