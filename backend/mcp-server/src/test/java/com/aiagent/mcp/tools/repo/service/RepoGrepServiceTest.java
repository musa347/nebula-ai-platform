package com.aiagent.mcp.tools.repo.service;

import com.aiagent.common.dto.RepoGrepRequest;
import com.aiagent.common.dto.RepoGrepResponse;
import com.aiagent.common.model.SearchMatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class RepoGrepServiceTest {

    private RepoGrepService service;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        service = new RepoGrepService();
    }

    @Test
    void testExactTextMatch() throws IOException {
        // Given
        Path file = tempDir.resolve("Test.java");
        Files.writeString(file, "public class WebClient {\n    private String url;\n}");

        // When
        RepoGrepRequest request = new RepoGrepRequest("WebClient");
        RepoGrepResponse response = service.grep(request, tempDir.toString());

        // Then
        assertEquals(1, response.getMatches().size());
        SearchMatch match = response.getMatches().get(0);
        assertTrue(match.getFile().endsWith("Test.java"));
        assertEquals(1, match.getLineNumber());
        assertTrue(match.getSnippet().contains("WebClient"));
    }

    @Test
    void testPartialTextMatch() throws IOException {
        // Given
        Path file = tempDir.resolve("Service.java");
        Files.writeString(file, "public class UserService {\n    public void save() {}\n}");

        // When
        RepoGrepRequest request = new RepoGrepRequest("Service");
        RepoGrepResponse response = service.grep(request, tempDir.toString());

        // Then
        assertEquals(1, response.getMatches().size());
        assertTrue(response.getMatches().get(0).getSnippet().contains("Service"));
    }

    @Test
    void testCaseInsensitiveMatch() throws IOException {
        // Given
        Path file = tempDir.resolve("Test.java");
        Files.writeString(file, "public class WebClient {}");

        // When
        RepoGrepRequest request = new RepoGrepRequest("WEBCLIENT");
        RepoGrepResponse response = service.grep(request, tempDir.toString());

        // Then
        assertEquals(1, response.getMatches().size());
    }

    @Test
    void testReturnsCorrectLineNumber() throws IOException {
        // Given
        Path file = tempDir.resolve("Test.java");
        Files.writeString(file, "line 1\nline 2\nWebClient here\nline 4");

        // When
        RepoGrepRequest request = new RepoGrepRequest("WebClient");
        RepoGrepResponse response = service.grep(request, tempDir.toString());

        // Then
        assertEquals(1, response.getMatches().size());
        assertEquals(3, response.getMatches().get(0).getLineNumber());
    }

    @Test
    void testReturnsCorrectSnippet() throws IOException {
        // Given
        Path file = tempDir.resolve("Test.java");
        Files.writeString(file, "public class WebClient extends BaseClient {}");

        // When
        RepoGrepRequest request = new RepoGrepRequest("WebClient");
        RepoGrepResponse response = service.grep(request, tempDir.toString());

        // Then
        assertEquals(1, response.getMatches().size());
        assertEquals("public class WebClient extends BaseClient {}", response.getMatches().get(0).getSnippet());
    }

    @Test
    void testIgnoresBinaries() throws IOException {
        // Given
        Path javaFile = tempDir.resolve("Test.java");
        Path classFile = tempDir.resolve("Test.class");
        Files.writeString(javaFile, "WebClient");
        Files.writeString(classFile, "WebClient");

        // When
        RepoGrepRequest request = new RepoGrepRequest("WebClient");
        RepoGrepResponse response = service.grep(request, tempDir.toString());

        // Then
        assertEquals(1, response.getMatches().size());
        assertTrue(response.getMatches().get(0).getFile().endsWith(".java"));
    }

    @Test
    void testIgnoresGitDirectory() throws IOException {
        // Given
        Path gitDir = tempDir.resolve(".git");
        Files.createDirectories(gitDir);
        Path gitFile = gitDir.resolve("config");
        Files.writeString(gitFile, "WebClient");
        
        Path normalFile = tempDir.resolve("Test.java");
        Files.writeString(normalFile, "WebClient");

        // When
        RepoGrepRequest request = new RepoGrepRequest("WebClient");
        RepoGrepResponse response = service.grep(request, tempDir.toString());

        // Then
        assertEquals(1, response.getMatches().size());
        assertFalse(response.getMatches().get(0).getFile().contains(".git"));
    }

    @Test
    void testHandlesEmptyResults() throws IOException {
        // Given
        Path file = tempDir.resolve("Test.java");
        Files.writeString(file, "public class Test {}");

        // When
        RepoGrepRequest request = new RepoGrepRequest("NonExistent");
        RepoGrepResponse response = service.grep(request, tempDir.toString());

        // Then
        assertEquals(0, response.getMatches().size());
    }

    @Test
    void testHandlesLargeFilesSafely() throws IOException {
        // Given
        Path file = tempDir.resolve("Large.java");
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            content.append("line ").append(i).append("\n");
        }
        content.append("WebClient found here\n");
        Files.writeString(file, content.toString());

        // When
        RepoGrepRequest request = new RepoGrepRequest("WebClient");
        RepoGrepResponse response = service.grep(request, tempDir.toString());

        // Then
        assertEquals(1, response.getMatches().size());
        assertEquals(10001, response.getMatches().get(0).getLineNumber());
    }

    @Test
    void testMultipleMatchesInSingleFile() throws IOException {
        // Given
        Path file = tempDir.resolve("Test.java");
        Files.writeString(file, "WebClient client1;\nString name;\nWebClient client2;");

        // When
        RepoGrepRequest request = new RepoGrepRequest("WebClient");
        RepoGrepResponse response = service.grep(request, tempDir.toString());

        // Then
        assertEquals(2, response.getMatches().size());
        assertEquals(1, response.getMatches().get(0).getLineNumber());
        assertEquals(3, response.getMatches().get(1).getLineNumber());
    }

    @Test
    void testMultipleMatchesAcrossFiles() throws IOException {
        // Given
        Path file1 = tempDir.resolve("Test1.java");
        Path file2 = tempDir.resolve("Test2.java");
        Files.writeString(file1, "WebClient client;");
        Files.writeString(file2, "WebClient other;");

        // When
        RepoGrepRequest request = new RepoGrepRequest("WebClient");
        RepoGrepResponse response = service.grep(request, tempDir.toString());

        // Then
        assertEquals(2, response.getMatches().size());
    }
}
