package com.aiagent.mcp.tools.repo.service;

import com.aiagent.common.dto.SymbolSearchRequest;
import com.aiagent.common.dto.SymbolSearchResponse;
import com.aiagent.common.model.SymbolMatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SymbolSearchServiceTest {

    private SymbolSearchService service;
    private JavaParserService javaParserService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        javaParserService = new JavaParserService();
        service = new SymbolSearchService(javaParserService);
    }

    @Test
    void testFindClassByName() throws IOException {
        // Given
        Path file = tempDir.resolve("UserService.java");
        Files.writeString(file, """
            package com.example;
            public class UserService {
                public void save() {}
            }
        """);

        // When
        SymbolSearchRequest request = new SymbolSearchRequest("UserService");
        SymbolSearchResponse response = service.search(request, tempDir.toString());

        // Then
        assertFalse(response.getMatches().isEmpty());
        SymbolMatch classMatch = response.getMatches().stream()
                .filter(m -> m.getMethodName() == null)
                .findFirst()
                .orElse(null);
        assertNotNull(classMatch);
        assertEquals("UserService", classMatch.getClassName());
    }

    @Test
    void testFindMethodByName() throws IOException {
        // Given
        Path file = tempDir.resolve("UserService.java");
        Files.writeString(file, """
            public class UserService {
                public void getUserById() {}
                public void save() {}
            }
        """);

        // When
        SymbolSearchRequest request = new SymbolSearchRequest("getUserById");
        SymbolSearchResponse response = service.search(request, tempDir.toString());

        // Then
        assertFalse(response.getMatches().isEmpty());
        SymbolMatch methodMatch = response.getMatches().stream()
                .filter(m -> "getUserById".equals(m.getMethodName()))
                .findFirst()
                .orElse(null);
        assertNotNull(methodMatch);
        assertEquals("UserService", methodMatch.getClassName());
        assertEquals("getUserById", methodMatch.getMethodName());
    }

    @Test
    void testPartialMatch() throws IOException {
        // Given
        Path file = tempDir.resolve("UserService.java");
        Files.writeString(file, """
            public class UserService {
                public void getUserById() {}
            }
        """);

        // When
        SymbolSearchRequest request = new SymbolSearchRequest("User");
        SymbolSearchResponse response = service.search(request, tempDir.toString());

        // Then
        assertFalse(response.getMatches().isEmpty());
        assertTrue(response.getMatches().stream()
                .anyMatch(m -> m.getClassName().contains("User")));
    }

    @Test
    void testCaseInsensitiveMatch() throws IOException {
        // Given
        Path file = tempDir.resolve("UserService.java");
        Files.writeString(file, """
            public class UserService {
                public void getUserById() {}
            }
        """);

        // When
        SymbolSearchRequest request = new SymbolSearchRequest("USERSERVICE");
        SymbolSearchResponse response = service.search(request, tempDir.toString());

        // Then
        assertFalse(response.getMatches().isEmpty());
    }

    @Test
    void testMultipleResults() throws IOException {
        // Given
        Path file1 = tempDir.resolve("UserService.java");
        Files.writeString(file1, """
            public class UserService {
                public void getUser() {}
            }
        """);

        Path file2 = tempDir.resolve("UserRepository.java");
        Files.writeString(file2, """
            public class UserRepository {
                public void findUser() {}
            }
        """);

        // When
        SymbolSearchRequest request = new SymbolSearchRequest("User");
        SymbolSearchResponse response = service.search(request, tempDir.toString());

        // Then
        assertTrue(response.getMatches().size() >= 2);
    }

    @Test
    void testNoMatches() throws IOException {
        // Given
        Path file = tempDir.resolve("UserService.java");
        Files.writeString(file, """
            public class UserService {
                public void save() {}
            }
        """);

        // When
        SymbolSearchRequest request = new SymbolSearchRequest("NonExistent");
        SymbolSearchResponse response = service.search(request, tempDir.toString());

        // Then
        assertTrue(response.getMatches().isEmpty());
    }

    @Test
    void testLargeRepoSafe() throws IOException {
        // Given - create 100 files
        for (int i = 0; i < 100; i++) {
            Path file = tempDir.resolve("Service" + i + ".java");
            Files.writeString(file, """
                public class Service%d {
                    public void method%d() {}
                }
            """.formatted(i, i));
        }

        // When
        SymbolSearchRequest request = new SymbolSearchRequest("Service");
        SymbolSearchResponse response = service.search(request, tempDir.toString());

        // Then
        assertEquals(100, response.getMatches().size());
    }

    @Test
    void testIgnoresTargetDirectory() throws IOException {
        // Given
        Path targetDir = tempDir.resolve("target");
        Files.createDirectories(targetDir);
        Path targetFile = targetDir.resolve("UserService.java");
        Files.writeString(targetFile, """
            public class UserService {
                public void save() {}
            }
        """);

        Path srcFile = tempDir.resolve("UserService.java");
        Files.writeString(srcFile, """
            public class UserService {
                public void save() {}
            }
        """);

        // When
        SymbolSearchRequest request = new SymbolSearchRequest("UserService");
        SymbolSearchResponse response = service.search(request, tempDir.toString());

        // Then
        assertEquals(1, response.getMatches().size());
        assertFalse(response.getMatches().get(0).getFile().contains("target"));
    }
}
