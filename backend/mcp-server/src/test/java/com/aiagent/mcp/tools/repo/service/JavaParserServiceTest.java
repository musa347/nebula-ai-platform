package com.aiagent.mcp.tools.repo.service;

import com.aiagent.common.model.ClassMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class JavaParserServiceTest {

    private JavaParserService service;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        service = new JavaParserService();
    }

    @Test
    void testSimpleClassParsing() throws IOException {
        // Given
        Path file = tempDir.resolve("UserService.java");
        Files.writeString(file, """
            package com.example;
            
            public class UserService {
                public User getUserById(Long id) {
                    return null;
                }
            }
        """);

        // When
        ClassMetadata metadata = service.extractMetadata(file);

        // Then
        assertNotNull(metadata);
        assertEquals("UserService", metadata.getClassName());
        assertEquals("com.example", metadata.getPackageName());
        assertEquals(1, metadata.getMethods().size());
        assertEquals("getUserById", metadata.getMethods().get(0));
    }

    @Test
    void testMultipleMethods() throws IOException {
        // Given
        Path file = tempDir.resolve("UserService.java");
        Files.writeString(file, """
            public class UserService {
                public void save() {}
                public void delete() {}
                public void update() {}
            }
        """);

        // When
        ClassMetadata metadata = service.extractMetadata(file);

        // Then
        assertNotNull(metadata);
        assertEquals(3, metadata.getMethods().size());
        assertTrue(metadata.getMethods().contains("save"));
        assertTrue(metadata.getMethods().contains("delete"));
        assertTrue(metadata.getMethods().contains("update"));
    }

    @Test
    void testInterfaceParsing() throws IOException {
        // Given
        Path file = tempDir.resolve("UserRepository.java");
        Files.writeString(file, """
            package com.example.repo;
            
            public interface UserRepository {
                User findById(Long id);
                void save(User user);
            }
        """);

        // When
        ClassMetadata metadata = service.extractMetadata(file);

        // Then
        assertNotNull(metadata);
        assertEquals("UserRepository", metadata.getClassName());
        assertEquals("com.example.repo", metadata.getPackageName());
        assertEquals(2, metadata.getMethods().size());
    }

    @Test
    void testEmptyClass() throws IOException {
        // Given
        Path file = tempDir.resolve("EmptyClass.java");
        Files.writeString(file, """
            public class EmptyClass {
            }
        """);

        // When
        ClassMetadata metadata = service.extractMetadata(file);

        // Then
        assertNotNull(metadata);
        assertEquals("EmptyClass", metadata.getClassName());
        assertEquals(0, metadata.getMethods().size());
    }

    @Test
    void testInvalidSyntaxSafeFailure() throws IOException {
        // Given
        Path file = tempDir.resolve("Invalid.java");
        Files.writeString(file, """
            public class Invalid {
                this is not valid java syntax
            }
        """);

        // When
        ClassMetadata metadata = service.extractMetadata(file);

        // Then
        assertNull(metadata);
    }

    @Test
    void testNoPackageDeclaration() throws IOException {
        // Given
        Path file = tempDir.resolve("SimpleClass.java");
        Files.writeString(file, """
            public class SimpleClass {
                public void doSomething() {}
            }
        """);

        // When
        ClassMetadata metadata = service.extractMetadata(file);

        // Then
        assertNotNull(metadata);
        assertEquals("SimpleClass", metadata.getClassName());
        assertEquals("", metadata.getPackageName());
        assertEquals(1, metadata.getMethods().size());
    }

    @Test
    void testFileNotFound() {
        // Given
        Path nonExistent = tempDir.resolve("DoesNotExist.java");

        // When
        ClassMetadata metadata = service.extractMetadata(nonExistent);

        // Then
        assertNull(metadata);
    }
}
