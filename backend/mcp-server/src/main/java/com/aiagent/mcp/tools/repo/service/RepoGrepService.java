package com.aiagent.mcp.tools.repo.service;

import com.aiagent.common.dto.RepoGrepRequest;
import com.aiagent.common.dto.RepoGrepResponse;
import com.aiagent.common.model.SearchMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class RepoGrepService {

    private static final Logger log = LoggerFactory.getLogger(RepoGrepService.class);
    private static final List<String> IGNORED_DIRS = List.of(".git", "target", "build", "node_modules", ".idea");
    private static final List<String> BINARY_EXTENSIONS = List.of(".class", ".jar", ".war", ".zip", ".png", ".jpg", ".gif");

    public RepoGrepResponse grep(RepoGrepRequest request, String workspacePath) {
        String query = request.getQuery().toLowerCase();
        Path root = Paths.get(workspacePath);
        List<SearchMatch> matches = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile)
                 .filter(path -> !isIgnored(path))
                 .filter(path -> !isBinary(path))
                 .forEach(path -> searchInFile(path, query, matches));

            log.info("Found {} matches for query: {}", matches.size(), request.getQuery());
            return new RepoGrepResponse(matches);
        } catch (IOException e) {
            log.error("Error grepping repository", e);
            return new RepoGrepResponse(List.of());
        }
    }

    private void searchInFile(Path path, String query, List<SearchMatch> matches) {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains(query)) {
                    matches.add(new SearchMatch(path.toString(), lineNumber, line.trim()));
                }
                lineNumber++;
            }
        } catch (IOException e) {
            log.debug("Could not read file: {}", path, e);
        }
    }

    private boolean isIgnored(Path path) {
        return IGNORED_DIRS.stream()
                .anyMatch(ignored -> path.toString().contains("/" + ignored + "/") || 
                                    path.toString().contains("\\" + ignored + "\\"));
    }

    private boolean isBinary(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        return BINARY_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }
}
