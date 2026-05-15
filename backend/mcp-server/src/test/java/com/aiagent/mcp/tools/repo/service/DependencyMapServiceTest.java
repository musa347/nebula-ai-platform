package com.aiagent.mcp.tools.repo.service;

import com.aiagent.common.model.DependencyEdge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DependencyMapServiceTest {

    private DependencyMapService service;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        service = new DependencyMapService();
    }

    @Test
    void testDetectImports() throws IOException {
        // Given
        Path file = tempDir.resolve("UserService.java");
        Files.writeString(file, """
            package com.example;
            
            import com.example.repository.UserRepository;
            import com.example.model.User;
            
            public class UserService {
            }
        """);

        // When
        List<DependencyEdge> edges = service.extractDependencies(file);

        // Then
        List<DependencyEdge> imports = edges.stream()
                .filter(e -> "IMPORT".equals(e.getType()))
                .toList();
        assertEquals(2, imports.size());
        assertTrue(imports.stream().anyMatch(e -> "UserRepository".equals(e.getToClass())));
        assertTrue(imports.stream().anyMatch(e -> "User".equals(e.getToClass())));
    }

    @Test
    void testDetectConstructorInjection() throws IOException {
        // Given
        Path file = tempDir.resolve("UserService.java");
        Files.writeString(file, """
            public class UserService {
                public UserService(UserRepository repository, CacheService cache) {
                }
            }
        """);

        // When
        List<DependencyEdge> edges = service.extractDependencies(file);

        // Then
        List<DependencyEdge> injections = edges.stream()
                .filter(e -> "INJECTION".equals(e.getType()))
                .toList();
        assertEquals(2, injections.size());
        assertTrue(injections.stream().anyMatch(e -> "UserRepository".equals(e.getToClass())));
        assertTrue(injections.stream().anyMatch(e -> "CacheService".equals(e.getToClass())));
    }

    @Test
    void testDetectFieldInjection() throws IOException {
        // Given
        Path file = tempDir.resolve("UserService.java");
        Files.writeString(file, """
            public class UserService {
                private UserRepository repository;
                private CacheService cache;
            }
        """);

        // When
        List<DependencyEdge> edges = service.extractDependencies(file);

        // Then
        List<DependencyEdge> fields = edges.stream()
                .filter(e -> "FIELD".equals(e.getType()))
                .toList();
        assertEquals(2, fields.size());
        assertTrue(fields.stream().anyMatch(e -> "UserRepository".equals(e.getToClass())));
        assertTrue(fields.stream().anyMatch(e -> "CacheService".equals(e.getToClass())));
    }

    @Test
    void testMultipleDependenciesPerClass() throws IOException {
        // Given
        Path file = tempDir.resolve("UserService.java");
        Files.writeString(file, """
            package com.example;
            
            import com.example.repository.UserRepository;
            
            public class UserService {
                private CacheService cache;
                
                public UserService(EmailService emailService) {
                }
            }
        """);

        // When
        List<DependencyEdge> edges = service.extractDependencies(file);

        // Then
        assertEquals(3, edges.size());
        assertTrue(edges.stream().anyMatch(e -> "IMPORT".equals(e.getType())));
        assertTrue(edges.stream().anyMatch(e -> "FIELD".equals(e.getType())));
        assertTrue(edges.stream().anyMatch(e -> "INJECTION".equals(e.getType())));
    }

    @Test
    void testNoDependenciesCase() throws IOException {
        // Given
        Path file = tempDir.resolve("EmptyClass.java");
        Files.writeString(file, """
            public class EmptyClass {
            }
        """);

        // When
        List<DependencyEdge> edges = service.extractDependencies(file);

        // Then
        assertEquals(0, edges.size());
    }

    @Test
    void testLargeFileSafe() throws IOException {
        // Given
        Path file = tempDir.resolve("LargeClass.java");
        StringBuilder content = new StringBuilder("public class LargeClass {\n");
        for (int i = 0; i < 100; i++) {
            content.append("    private Service").append(i).append(" service").append(i).append(";\n");
        }
        content.append("}");
        Files.writeString(file, content.toString());

        // When
        List<DependencyEdge> edges = service.extractDependencies(file);

        // Then
        assertEquals(100, edges.size());
        assertTrue(edges.stream().allMatch(e -> "FIELD".equals(e.getType())));
    }

    @Test
    void testInvalidFileSafe() throws IOException {
        // Given
        Path file = tempDir.resolve("Invalid.java");
        Files.writeString(file, """
            public class Invalid {
                this is not valid java
            }
        """);

        // When
        List<DependencyEdge> edges = service.extractDependencies(file);

        // Then
        assertEquals(0, edges.size());
    }

    @Test
    void testExtractSimpleClassName() throws IOException {
        // Given
        Path file = tempDir.resolve("UserService.java");
        Files.writeString(file, """
            import com.example.repository.UserRepository;
            
            public class UserService {
            }
        """);

        // When
        List<DependencyEdge> edges = service.extractDependencies(file);

        // Then
        DependencyEdge importEdge = edges.stream()
                .filter(e -> "IMPORT".equals(e.getType()))
                .findFirst()
                .orElse(null);
        assertNotNull(importEdge);
        assertEquals("UserRepository", importEdge.getToClass());
        assertEquals("UserService", importEdge.getFromClass());
    }

    @Test
    void testFileNotFound() {
        // Given
        Path nonExistent = tempDir.resolve("DoesNotExist.java");

        // When
        List<DependencyEdge> edges = service.extractDependencies(nonExistent);

        // Then
        assertEquals(0, edges.size());
    }
}
