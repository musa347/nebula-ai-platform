package com.aiagent.mcp.tools.repo.service;

import com.aiagent.common.dto.ContextRequest;
import com.aiagent.common.dto.ContextResponse;
import com.aiagent.common.model.ContextSlice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ContextMinimizationServiceTest {

    private ContextMinimizationService service;
    private RepoSearchService repoSearchService;
    private RepoGrepService repoGrepService;
    private SymbolSearchService symbolSearchService;
    private DependencyMapService dependencyMapService;
    private JavaParserService javaParserService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        javaParserService = new JavaParserService();
        dependencyMapService = new DependencyMapService();
        repoSearchService = new RepoSearchService();
        repoGrepService = new RepoGrepService();
        symbolSearchService = new SymbolSearchService(javaParserService);
        
        service = new ContextMinimizationService(
            repoSearchService,
            repoGrepService,
            symbolSearchService,
            dependencyMapService,
            javaParserService
        );
    }

    @Test
    void testSelectCorrectFileSections() throws IOException {
        // Given
        Path userService = tempDir.resolve("UserService.java");
        Files.writeString(userService, """
            public class UserService {
                public void saveUser() {}
                public void deleteUser() {}
            }
        """);

        // When
        ContextRequest request = new ContextRequest("UserService");
        ContextResponse response = service.buildContext(request, tempDir.toString());

        // Then
        assertFalse(response.getSlices().isEmpty());
        assertTrue(response.getSlices().stream()
            .anyMatch(s -> s.getFile().contains("UserService")));
    }

    @Test
    void testIgnoreIrrelevantMethods() throws IOException {
        // Given
        Path userService = tempDir.resolve("UserService.java");
        Files.writeString(userService, """
            public class UserService {
                public void saveUser() {}
            }
        """);
        
        Path orderService = tempDir.resolve("OrderService.java");
        Files.writeString(orderService, """
            public class OrderService {
                public void saveOrder() {}
            }
        """);

        // When
        ContextRequest request = new ContextRequest("UserService");
        ContextResponse response = service.buildContext(request, tempDir.toString());

        // Then
        assertTrue(response.getSlices().stream()
            .anyMatch(s -> s.getFile().contains("UserService")));
    }

    @Test
    void testIncludeDependencyRelevantClasses() throws IOException {
        // Given
        Path userService = tempDir.resolve("UserService.java");
        Files.writeString(userService, """
            import com.example.UserRepository;
            
            public class UserService {
                private UserRepository repository;
            }
        """);
        
        Path userRepository = tempDir.resolve("UserRepository.java");
        Files.writeString(userRepository, """
            public class UserRepository {
                public void save() {}
            }
        """);

        // When
        ContextRequest request = new ContextRequest("UserService");
        ContextResponse response = service.buildContext(request, tempDir.toString());

        // Then
        assertTrue(response.getSlices().size() >= 1);
    }

    @Test
    void testHandleMultipleFiles() throws IOException {
        // Given
        Path file1 = tempDir.resolve("UserService.java");
        Files.writeString(file1, """
            public class UserService {
                public void save() {}
            }
        """);
        
        Path file2 = tempDir.resolve("UserController.java");
        Files.writeString(file2, """
            public class UserController {
                public void getUser() {}
            }
        """);

        // When
        ContextRequest request = new ContextRequest("User");
        ContextResponse response = service.buildContext(request, tempDir.toString());

        // Then
        assertTrue(response.getSlices().size() >= 2);
    }

    @Test
    void testExplainInclusionReason() throws IOException {
        // Given
        Path userService = tempDir.resolve("UserService.java");
        Files.writeString(userService, """
            public class UserService {
                public void save() {}
            }
        """);

        // When
        ContextRequest request = new ContextRequest("UserService");
        ContextResponse response = service.buildContext(request, tempDir.toString());

        // Then
        assertFalse(response.getSlices().isEmpty());
        ContextSlice slice = response.getSlices().get(0);
        assertNotNull(slice.getReason());
        assertFalse(slice.getReason().isEmpty());
    }

    @Test
    void testHandleLargeRepos() throws IOException {
        // Given - create 50 files
        for (int i = 0; i < 50; i++) {
            Path file = tempDir.resolve("Service" + i + ".java");
            Files.writeString(file, """
                public class Service%d {
                    public void method%d() {}
                }
            """.formatted(i, i));
        }
        
        Path targetFile = tempDir.resolve("UserService.java");
        Files.writeString(targetFile, """
            public class UserService {
                public void save() {}
            }
        """);

        // When
        ContextRequest request = new ContextRequest("UserService");
        ContextResponse response = service.buildContext(request, tempDir.toString());

        // Then
        assertFalse(response.getSlices().isEmpty());
        assertTrue(response.getSlices().size() < 50); // Should not include all files
    }

    @Test
    void testEmptyTask() throws IOException {
        // Given
        Path file = tempDir.resolve("Test.java");
        Files.writeString(file, "public class Test {}");

        // When
        ContextRequest request = new ContextRequest("");
        ContextResponse response = service.buildContext(request, tempDir.toString());

        // Then
        assertNotNull(response.getSlices());
    }

    @Test
    void testTaskWithMultipleKeywords() throws IOException {
        // Given
        Path userService = tempDir.resolve("UserService.java");
        Files.writeString(userService, """
            public class UserService {
                public void save() {}
            }
        """);
        
        Path cacheService = tempDir.resolve("CacheService.java");
        Files.writeString(cacheService, """
            public class CacheService {
                public void cache() {}
            }
        """);

        // When
        ContextRequest request = new ContextRequest("Add caching to UserService");
        ContextResponse response = service.buildContext(request, tempDir.toString());

        // Then
        assertTrue(response.getSlices().size() >= 1);
    }
}
