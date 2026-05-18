FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="AI Agent Platform"
LABEL description="Agent CLI - Command-line interface for AI Agent Platform"

WORKDIR /app

# Copy the JAR file
COPY target/agent-cli-*.jar app.jar

# Create a non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Set entrypoint
ENTRYPOINT ["java", "-jar", "app.jar"]

# Default command (can be overridden)
CMD ["run", "help"]
