package com.aiagent.mcp.tools.repo.service;

import com.aiagent.common.model.DependencyEdge;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class DependencyMapService {

    private static final Logger log = LoggerFactory.getLogger(DependencyMapService.class);
    private final JavaParser javaParser = new JavaParser();

    public List<DependencyEdge> extractDependencies(Path filePath) {
        List<DependencyEdge> edges = new ArrayList<>();

        try {
            String content = Files.readString(filePath);
            ParseResult<CompilationUnit> parseResult = javaParser.parse(content);

            if (!parseResult.isSuccessful() || parseResult.getResult().isEmpty()) {
                log.warn("Failed to parse file: {}", filePath);
                return edges;
            }

            CompilationUnit cu = parseResult.getResult().get();
            ClassOrInterfaceDeclaration classDecl = cu.findFirst(ClassOrInterfaceDeclaration.class).orElse(null);

            if (classDecl == null) {
                return edges;
            }

            String fromClass = classDecl.getNameAsString();

            // Extract imports
            for (ImportDeclaration importDecl : cu.getImports()) {
                String importName = importDecl.getNameAsString();
                String toClass = extractSimpleClassName(importName);
                edges.add(new DependencyEdge(fromClass, toClass, "IMPORT"));
            }

            // Extract field dependencies
            for (FieldDeclaration field : classDecl.getFields()) {
                String fieldType = field.getCommonType().asString();
                String toClass = extractSimpleClassName(fieldType);
                edges.add(new DependencyEdge(fromClass, toClass, "FIELD"));
            }

            // Extract constructor injection
            for (ConstructorDeclaration constructor : classDecl.getConstructors()) {
                for (Parameter param : constructor.getParameters()) {
                    String paramType = param.getType().asString();
                    String toClass = extractSimpleClassName(paramType);
                    edges.add(new DependencyEdge(fromClass, toClass, "INJECTION"));
                }
            }

            log.debug("Extracted {} dependencies from {}", edges.size(), fromClass);
            return edges;

        } catch (IOException e) {
            log.error("Error reading file: {}", filePath, e);
            return edges;
        } catch (Exception e) {
            log.error("Error parsing file: {}", filePath, e);
            return edges;
        }
    }

    private String extractSimpleClassName(String fullName) {
        if (fullName.contains(".")) {
            return fullName.substring(fullName.lastIndexOf('.') + 1);
        }
        return fullName;
    }
}
