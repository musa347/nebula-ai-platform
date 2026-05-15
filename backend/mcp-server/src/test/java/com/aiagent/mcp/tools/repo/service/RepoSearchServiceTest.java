package com.aiagent.mcp.tools.repo.service;

import com.aiagent.common.dto.RepoSearchRequest;
import com.aiagent.common.dto.RepoSearchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RepoSearchServiceTest {

    private RepoSearchService service;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        service = new RepoSearchService();
    }

    @Test
    void testExactFilenameMatch() throws IOException {
        // Given
        Files.createFile(tempDir.resolve("UserService.java"));
        Files.createFile(tempDir.resolve("OrderService.java"));

        // When
        RepoSearchRequest request = new RepoSearchRequest("UserService.java");
        RepoSearchResponse response = service.search(request, tempDir.toString());

        // Then
        assertEquals(1, response.getFiles().size());
        assertTrue(response.getFiles().get(0).endsWith("UserService.java"));
    }

    @Test
    void testPartialFilenameMatch() throws IOException {
        // Given
        Files.createFile(tempDir.resolve("UserService.java"));
        Files.createFile(tempDir.resolve("UserServiceTest.java"));
        Files.createFile(tempDir.resolve("UserServiceImpl.java"));
        Files.createFile(tempDir.resolve("OrderService.java"));

        // When
        RepoSearchRequest request = new RepoSearchRequest("UserService");
        RepoSearchResponse response = service.search(request, tempDir.toString());

        // Then
        assertEquals(3, response.getFiles().size());
        assertTrue(response.getFiles().stream().allMatch(f -> f.contains("UserService")));
    }

    @Test
    void testCaseInsensitiveMatch() throws IOException {
        // Given
        Files.createFile(tempDir.resolve("UserService.java"));
        Files.createFile(tempDir.resolve("ProductService.java"));

        // When
        RepoSearchRequest request = new RepoSearchRequest("USERSERVICE");
        RepoSearchResponse response = service.search(request, tempDir.toString());

        // Then
        assertEquals(1, response.getFiles().size());
        assertTrue(response.getFiles().get(0).contains("UserService"));
    }

    @Test
    void testIgnoreGitDirectory() throws IOException {
        // Given
        Path gitDir = tempDir.resolve(".git");
        Files.createDirectories(gitDir);
        Files.createFile(gitDir.resolve("config"));
        Files.createFile(tempDir.resolve("UserService.java"));

        // When
        RepoSearchRequest request = new RepoSearchRequest("config");
        RepoSearchResponse response = service.search(request, tempDir.toString());

        // Then
        assertEquals(0, response.getFiles().size());
    }

    @Test
    void testIgnoreTargetDirectory() throws IOException {
        // Given
        Path targetDir = tempDir.resolve("target");
        Files.createDirectories(targetDir);
        Files.createFile(targetDir.resolve("UserService.class"));
        Files.createFile(tempDir.resolve("UserService.java"));

        // When
        RepoSearchRequest request = new RepoSearchRequest("UserService");
        RepoSearchResponse response = service.search(request, tempDir.toString());

        // Then
        assertEquals(1, response.getFiles().size());
        assertTrue(response.getFiles().get(0).endsWith(".java"));
    }

    @Test
    void testNestedDirectories() throws IOException {
        // Given
        Path srcDir = tempDir.resolve("src/main/java/com/example");
        Files.createDirectories(srcDir);
        Files.createFile(srcDir.resolve("UserService.java"));

        // When
        RepoSearchRequest request = new RepoSearchRequest("UserService");
        RepoSearchResponse response = service.search(request, tempDir.toString());

        // Then
        assertEquals(1, response.getFiles().size());
        assertTrue(response.getFiles().get(0).contains("UserService.java"));
    }

    @Test
    void testNoMatches() throws IOException {
        // Given
        Files.createFile(tempDir.resolve("UserService.java"));

        // When
        RepoSearchRequest request = new RepoSearchRequest("NonExistent");
        RepoSearchResponse response = service.search(request, tempDir.toString());

        // Then
        assertEquals(0, response.getFiles().size());
    }

    @Test
    void testMultipleIgnoredDirectories() throws IOException {
        // Given
        Path gitDir = tempDir.resolve(".git");
        Path targetDir = tempDir.resolve("target");
        Path buildDir = tempDir.resolve("build");
        Path nodeModules = tempDir.resolve("node_modules");

        Files.createDirectories(gitDir);
        Files.createDirectories(targetDir);
        Files.createDirectories(buildDir);
        Files.createDirectories(nodeModules);

        Files.createFile(gitDir.resolve("test.txt"));
        Files.createFile(targetDir.resolve("test.txt"));
        Files.createFile(buildDir.resolve("test.txt"));
        Files.createFile(nodeModules.resolve("test.txt"));
        Files.createFile(tempDir.resolve("test.txt"));

        // When
        RepoSearchRequest request = new RepoSearchRequest("test");
        RepoSearchResponse response = service.search(request, tempDir.toString());

        // Then
        assertEquals(1, response.getFiles().size());
        assertFalse(response.getFiles().get(0).contains(".git"));
        assertFalse(response.getFiles().get(0).contains("target"));
        assertFalse(response.getFiles().get(0).contains("build"));
        assertFalse(response.getFiles().get(0).contains("node_modules"));
    }
}
