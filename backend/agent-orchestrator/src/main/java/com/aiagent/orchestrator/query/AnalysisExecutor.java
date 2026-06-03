package com.aiagent.orchestrator.query;

import com.aiagent.common.model.LoadedContext;
import com.aiagent.orchestrator.service.ContextLoaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class AnalysisExecutor {

    private static final Logger log = LoggerFactory.getLogger(AnalysisExecutor.class);

    @Autowired
    private ContextLoaderService contextLoaderService;

    @Autowired(required = false)
    private ChatClient.Builder chatClientBuilder;

    @Autowired(required = false)
    private com.aiagent.orchestrator.service.HybridAiService hybridAiService;

    @Value("${spring.ai.ollama.chat.model:qwen2.5-coder:0.5b}")
    private String ollamaModel;

    public AnalysisResult analyze(String task, String workspacePath, List<String> workspaceFiles) {
        AnalysisResult result = new AnalysisResult();

        List<LoadedContext> contexts = contextLoaderService.loadContext(task, null, workspaceFiles);
        log.info("Loaded {} files for analysis", contexts.size());

        if (contexts.isEmpty() && workspaceFiles != null && !workspaceFiles.isEmpty()) {
            log.info("No specific files matched, searching all {} workspace files", workspaceFiles.size());
            contexts = findRelevantFilesForQuery(task, workspaceFiles);
            log.info("Found {} relevant files after search", contexts.size());
        }

        java.util.Set<String> seenFiles = new java.util.HashSet<>();
        List<LoadedContext> uniqueContexts = new java.util.ArrayList<>();
        for (LoadedContext ctx : contexts) {
            if (seenFiles.add(ctx.getFile())) {
                uniqueContexts.add(ctx);
            }
        }

        log.info("After deduplication: {} unique files", uniqueContexts.size());

        StringBuilder codeContent = new StringBuilder();
        for (LoadedContext context : uniqueContexts) {
            String filePath = resolveFilePath(workspacePath, context.getFile());
            log.info("Reading file: {}", filePath);
            try {
                String content = readFile(filePath);
                if (content != null && !content.isEmpty()) {
                    log.info("Read {} characters from {}", content.length(), context.getFile());
                    codeContent.append("\n\n=== ").append(context.getFile()).append(" ===\n");
                    codeContent.append(content);
                    result.addFile(context.getFile());
                } else {
                    log.warn("File is empty or null: {}", filePath);
                }
            } catch (Exception e) {
                log.warn("Failed to read file: {}", filePath, e);
            }
        }

        String explanation;
        if (codeContent.length() > 100) {
            explanation = generateAiAnalysis(task, codeContent.toString());
        } else {
            explanation = generateRuleBasedAnalysis(task, uniqueContexts, codeContent.toString());
        }
        log.info("Generated analysis: {} characters", explanation.length());
        result.setDetailedExplanation(explanation);

        extractKeyFindings(result, uniqueContexts);

        return result;
    }

    private String resolveFilePath(String workspacePath, String file) {
        if (file.startsWith("/") || file.contains(":")) {
            return file;
        }
        if (workspacePath != null) {
            return Paths.get(workspacePath, file).toString();
        }
        return file;
    }

    private String readFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                return Files.readString(path);
            }
        } catch (IOException e) {
            log.warn("Failed to read file: {}", filePath);
        }
        return null;
    }

    private String generateAiAnalysis(String task, String codeContent) {
        if (hybridAiService != null) {
            try {
                String relevantContent = extractRelevantContent(task, codeContent);
                String limitedContent = relevantContent.substring(0, Math.min(relevantContent.length(), 50000));

                String prompt = String.format("""
                        Question: %s
                        
                        Files content:
                        %s
                        
                        Search for database credentials including:
                        - spring.datasource.username
                        - spring.datasource.password
                        - spring.datasource.url
                        - jdbc connection strings
                        
                        Return the actual values found. Be specific and factual.
                        """, task, limitedContent);

                return hybridAiService.callWithFallback(prompt);
            } catch (Exception e) {
                log.error("HybridAI analysis failed: {}", e.getMessage());
            }
        }
        if (chatClientBuilder != null) {
            try {
                ChatClient chatClient = chatClientBuilder.build();
                String limitedContent = codeContent.substring(0, Math.min(codeContent.length(), 15000));

                String prompt = String.format("""
                        Question: %s
                        
                        Files content:
                        %s
                        
                        Analyze the files and answer the question directly and concisely.
                        """, task, limitedContent);

                return chatClient.prompt()
                        .options(org.springframework.ai.ollama.api.OllamaOptions.create()
                                .withModel(ollamaModel)
                                .withNumPredict(500))
                        .user(prompt)
                        .call()
                        .content();
            } catch (Exception e) {
                log.error("Ollama analysis failed: {}", e.getMessage());
            }
        }

        log.warn("No AI service available, falling back to rule-based analysis");
        return generateRuleBasedAnalysis(task, null, codeContent);
    }

    private String extractRelevantContent(String task, String codeContent) {
        String lowerTask = task.toLowerCase();
        if (lowerTask.contains("credential") || lowerTask.contains("password") || lowerTask.contains("username")) {
            // Extract only .properties and .yml sections
            StringBuilder relevant = new StringBuilder();
            String[] sections = codeContent.split("\n\n===");
            for (String section : sections) {
                if (section.contains(".properties") || section.contains(".yml") || section.contains(".yaml")) {
                    relevant.append("\n\n===").append(section);
                }
            }
            return relevant.length() > 0 ? relevant.toString() : codeContent;
        }
        return codeContent;
    }

    private String generateRuleBasedAnalysis(String task, List<LoadedContext> contexts, String codeContent) {
        StringBuilder analysis = new StringBuilder();

        boolean isSingleFile = contexts != null && contexts.size() <= 3;

        if (isSingleFile) {
            return generateDetailedFileAnalysis(contexts, codeContent);
        } else {
            return generateOverviewAnalysis(contexts, codeContent);
        }
    }

    private String generateDetailedFileAnalysis(List<LoadedContext> contexts, String codeContent) {
        StringBuilder analysis = new StringBuilder();

        analysis.append("### Detailed Code Analysis\n\n");

        java.util.regex.Pattern classPattern = java.util.regex.Pattern.compile(
                "(?:public|private|protected)?\\s*(?:abstract\\s+)?(?:final\\s+)?(class|interface|enum)\\s+(\\w+)(?:<[^>]+>)?(?:\\s+extends\\s+([\\w<>,\\s]+))?(?:\\s+implements\\s+([\\w<>,\\s]+))?");
        java.util.regex.Matcher classMatcher = classPattern.matcher(codeContent);

        java.util.List<String> classes = new java.util.ArrayList<>();
        while (classMatcher.find()) {
            String type = classMatcher.group(1);
            String name = classMatcher.group(2);
            String extendsClause = classMatcher.group(3);
            String implementsClause = classMatcher.group(4);

            StringBuilder classInfo = new StringBuilder("`").append(name).append("`");
            if (extendsClause != null) {
                classInfo.append(" extends `").append(extendsClause.trim()).append("`");
            }
            if (implementsClause != null) {
                classInfo.append(" implements `").append(implementsClause.trim()).append("`");
            }
            classes.add(classInfo.toString());
        }

        if (!classes.isEmpty()) {
            analysis.append("**Classes/Interfaces:**\n");
            for (String className : classes) {
                analysis.append("- ").append(className).append("\n");
            }
            analysis.append("\n");
        }

        java.util.regex.Pattern methodPattern = java.util.regex.Pattern.compile(
                "(?:public|private|protected)\\s+(?:static\\s+)?(?:final\\s+)?([\\w<>\\[\\],\\s]+)\\s+(\\w+)\\s*\\(([^)]*)\\)");
        java.util.regex.Matcher methodMatcher = methodPattern.matcher(codeContent);

        java.util.List<String> methods = new java.util.ArrayList<>();
        while (methodMatcher.find()) {
            String returnType = methodMatcher.group(1).trim();
            String methodName = methodMatcher.group(2);
            String params = methodMatcher.group(3).trim();

            if (!methodName.equals("class") && !methodName.equals("interface")) {
                StringBuilder methodSig = new StringBuilder("`").append(methodName).append("(");

                if (!params.isEmpty()) {
                    String[] paramArray = params.split(",");
                    for (int i = 0; i < paramArray.length; i++) {
                        String param = paramArray[i].trim();
                        String[] parts = param.split("\\s+");
                        if (parts.length >= 2) {
                            methodSig.append(parts[parts.length - 2]).append(" ").append(parts[parts.length - 1]);
                        } else if (parts.length == 1) {
                            methodSig.append(parts[0]);
                        }
                        if (i < paramArray.length - 1) methodSig.append(", ");
                    }
                }

                methodSig.append(")` → `").append(returnType).append("`");
                methods.add(methodSig.toString());
            }
        }

        if (!methods.isEmpty()) {
            analysis.append("**Methods (" + methods.size() + "):**\n");
            int count = 0;
            for (String method : methods) {
                if (count++ < 20) {
                    analysis.append("- ").append(method).append("\n");
                }
            }
            if (methods.size() > 20) {
                analysis.append("- ... and ").append(methods.size() - 20).append(" more\n");
            }
            analysis.append("\n");
        }

        java.util.regex.Pattern annotationPattern = java.util.regex.Pattern.compile("@(\\w+)");
        java.util.regex.Matcher annotationMatcher = annotationPattern.matcher(codeContent);

        java.util.Set<String> annotations = new java.util.HashSet<>();
        while (annotationMatcher.find()) {
            annotations.add(annotationMatcher.group(1));
        }

        if (!annotations.isEmpty()) {
            analysis.append("**Annotations Used:**\n");
            for (String annotation : annotations) {
                analysis.append("- @").append(annotation);

                switch (annotation) {
                    case "RestController" -> analysis.append(" - REST API endpoint handler");
                    case "Service" -> analysis.append(" - Business logic component");
                    case "Repository" -> analysis.append(" - Data access layer");
                    case "Async" -> analysis.append(" - Asynchronous execution");
                    case "Transactional" -> analysis.append(" - Database transaction management");
                    case "Autowired" -> analysis.append(" - Dependency injection");
                    case "RequestMapping", "GetMapping", "PostMapping", "PutMapping", "DeleteMapping" ->
                            analysis.append(" - HTTP endpoint mapping");
                    case "Valid" -> analysis.append(" - Input validation");
                }
                analysis.append("\n");
            }
            analysis.append("\n");
        }


        analysis.append("**Implementation Details:**\n");

        if (codeContent.contains("try") && codeContent.contains("catch")) {
            analysis.append("- Exception handling implemented\n");
        }

        if (codeContent.contains("log.")) {
            analysis.append("- Logging enabled\n");
        }

        if (codeContent.contains("RestTemplate") || codeContent.contains("WebClient")) {
            analysis.append("- Makes HTTP calls to external services\n");
        }

        if (codeContent.contains("@Transactional")) {
            analysis.append("- Database operations wrapped in transactions\n");
        }

        if (codeContent.contains("encrypt") || codeContent.contains("decrypt")) {
            analysis.append("- Handles encryption/decryption\n");
        }

        if (codeContent.contains("@Async")) {
            analysis.append("- Contains asynchronous methods (non-blocking)\n");
        }

        java.util.regex.Pattern depPattern = java.util.regex.Pattern.compile(
                "@Autowired[^;]*?\\s+(?:private\\s+)?([\\w<>]+)\\s+(\\w+);");
        java.util.regex.Matcher depMatcher = depPattern.matcher(codeContent);

        java.util.List<String> dependencies = new java.util.ArrayList<>();
        while (depMatcher.find()) {
            dependencies.add("`" + depMatcher.group(1) + "` (" + depMatcher.group(2) + ")");
        }
        java.util.regex.Pattern fieldPattern = java.util.regex.Pattern.compile(
                "private\\s+final\\s+([\\w<>]+)\\s+(\\w+);");
        java.util.regex.Matcher fieldMatcher = fieldPattern.matcher(codeContent);

        while (fieldMatcher.find()) {
            String dep = "`" + fieldMatcher.group(1) + "` (" + fieldMatcher.group(2) + ")";
            if (!dependencies.contains(dep)) {
                dependencies.add(dep);
            }
        }

        if (!dependencies.isEmpty()) {
            analysis.append("\n**Injected Dependencies:**\n");
            for (String dep : dependencies) {
                analysis.append("- ").append(dep).append("\n");
            }
        }

        return analysis.toString();
    }

    private String generateOverviewAnalysis(List<LoadedContext> contexts, String codeContent) {
        StringBuilder analysis = new StringBuilder();

        analysis.append("### Component Breakdown\n\n");

        if (contexts != null && !contexts.isEmpty()) {
            long controllers = contexts.stream().filter(c -> c.getFile().contains("controller")).count();
            long services = contexts.stream().filter(c -> c.getFile().contains("service")).count();
            long dtos = contexts.stream().filter(c -> c.getFile().contains("dto")).count();
            long entities = contexts.stream().filter(c -> c.getFile().contains("entity")).count();
            long repos = contexts.stream().filter(c -> c.getFile().contains("repo")).count();

            if (controllers > 0) analysis.append("- Controllers: ").append(controllers).append("\n");
            if (services > 0) analysis.append("- Services: ").append(services).append("\n");
            if (dtos > 0) analysis.append("- DTOs: ").append(dtos).append("\n");
            if (entities > 0) analysis.append("- Entities: ").append(entities).append("\n");
            if (repos > 0) analysis.append("- Repositories: ").append(repos).append("\n");
            analysis.append("\n");
        }

        analysis.append("### Architecture Patterns\n\n");

        if (codeContent.contains("@RestController")) {
            analysis.append("**REST API Layer:** Spring MVC controllers with HTTP endpoints\n");
        }

        if (codeContent.contains("@Service")) {
            analysis.append("**Service Layer:** Business logic encapsulation\n");
        }

        if (codeContent.contains("JpaRepository")) {
            analysis.append("**Data Access:** Spring Data JPA repositories\n");
        }

        analysis.append("\n### Key Features\n\n");

        if (codeContent.contains("@Async")) {
            analysis.append("- Asynchronous processing enabled\n");
        }

        if (codeContent.contains("@Transactional")) {
            analysis.append("- Transaction management (ACID guarantees)\n");
        }

        if (codeContent.contains("@Valid")) {
            analysis.append("- Input validation with Bean Validation\n");
        }

        if (codeContent.contains("RestTemplate")) {
            analysis.append("- External API integration\n");
        }

        if (codeContent.contains("encrypt") || codeContent.contains("Crypto")) {
            analysis.append("- Encryption/decryption capabilities\n");
        }

        analysis.append("\n### Summary\n\n");
        analysis.append("Spring Boot microservice with ");
        analysis.append(contexts != null ? contexts.size() : 0);
        analysis.append(" components following layered architecture. ");

        if (codeContent.contains("notification")) {
            analysis.append("Handles payment notifications and merchant onboarding.");
        }

        return analysis.toString();
    }

    private void extractKeyFindings(AnalysisResult result, List<LoadedContext> contexts) {
        if (contexts == null) return;

        long controllers = contexts.stream()
                .filter(c -> c.getFile().toLowerCase().contains("controller"))
                .count();

        long services = contexts.stream()
                .filter(c -> c.getFile().toLowerCase().contains("service"))
                .count();

        if (controllers > 0) {
            result.addFinding(controllers + " REST controller(s) found");
        }

        if (services > 0) {
            result.addFinding(services + " service layer component(s) found");
        }

        result.addFinding("Total files analyzed: " + contexts.size());
    }

    private List<LoadedContext> findRelevantFilesForQuery(String task, List<String> workspaceFiles) {
        List<LoadedContext> relevant = new java.util.ArrayList<>();
        String lowerTask = task.toLowerCase();

        log.info("Searching for files matching query: {}", lowerTask);
        log.info("Sample workspace files: {}", workspaceFiles.subList(0, Math.min(5, workspaceFiles.size())));

        if (lowerTask.contains("database") || lowerTask.contains("db") || lowerTask.contains("credential")) {
            for (String file : workspaceFiles) {
                String lower = file.toLowerCase();
                if (lower.contains("application") && (lower.endsWith(".yml") || lower.endsWith(".yaml") || lower.endsWith(".properties")) ||
                        lower.endsWith("pom.xml") || lower.endsWith("build.gradle") ||
                        lower.contains("datasource")) {
                    log.info("Matched database file: {}", file);
                    relevant.add(new LoadedContext(file, "Database-related file"));
                }
            }
        } else if (lowerTask.contains("api") || lowerTask.contains("endpoint") || lowerTask.contains("rest")) {
            for (String file : workspaceFiles) {
                String lower = file.toLowerCase();
                if (lower.contains("controller") || lower.contains("resource") ||
                        lower.contains("rest") || lower.contains("application.yml")) {
                    relevant.add(new LoadedContext(file, "API-related file"));
                }
            }
        } else if (lowerTask.contains("depend") || lowerTask.contains("library") || lowerTask.contains("package")) {
            for (String file : workspaceFiles) {
                String lower = file.toLowerCase();
                if (lower.contains("pom.xml") || lower.contains("build.gradle") ||
                        lower.contains("package.json") || lower.contains("requirements.txt")) {
                    relevant.add(new LoadedContext(file, "Dependency file"));
                }
            }
        } else if (lowerTask.contains("config") || lowerTask.contains("setting") || lowerTask.contains("property")) {
            for (String file : workspaceFiles) {
                String lower = file.toLowerCase();
                if (lower.contains("application") || lower.contains("config") ||
                        lower.contains(".properties") || lower.contains(".yml") || lower.contains(".yaml")) {
                    relevant.add(new LoadedContext(file, "Configuration file"));
                }
            }
        } else {
            for (String file : workspaceFiles) {
                String lower = file.toLowerCase();
                if (lower.contains("application") && (lower.endsWith(".yml") || lower.endsWith(".yaml") || lower.endsWith(".properties")) ||
                        lower.contains("pom.xml") || lower.contains("build.gradle")) {
                    relevant.add(new LoadedContext(file, "Configuration/build file"));
                }
            }
        }

        return relevant;
    }
}
