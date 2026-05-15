package com.aiagent.mcp.tools.repo.service;

import com.aiagent.common.model.ClassMetadata;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class JavaParserService {

    private static final Logger log = LoggerFactory.getLogger(JavaParserService.class);
    private final JavaParser javaParser = new JavaParser();

    public ClassMetadata extractMetadata(Path filePath) {
        try {
            String content = Files.readString(filePath);
            ParseResult<CompilationUnit> parseResult = javaParser.parse(content);

            if (!parseResult.isSuccessful() || parseResult.getResult().isEmpty()) {
                log.warn("Failed to parse file: {}", filePath);
                return null;
            }

            CompilationUnit cu = parseResult.getResult().get();
            Optional<ClassOrInterfaceDeclaration> classDecl = cu.findFirst(ClassOrInterfaceDeclaration.class);

            if (classDecl.isEmpty()) {
                return null;
            }

            ClassOrInterfaceDeclaration clazz = classDecl.get();
            String className = clazz.getNameAsString();
            String packageName = cu.getPackageDeclaration()
                    .map(pd -> pd.getNameAsString())
                    .orElse("");

            List<String> methods = clazz.getMethods().stream()
                    .map(MethodDeclaration::getNameAsString)
                    .toList();

            return new ClassMetadata(className, methods, packageName);

        } catch (IOException e) {
            log.error("Error reading file: {}", filePath, e);
            return null;
        } catch (Exception e) {
            log.error("Error parsing file: {}", filePath, e);
            return null;
        }
    }
}
