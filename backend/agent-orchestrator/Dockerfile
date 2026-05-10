FROM openjdk:21-jdk-slim

LABEL maintainer="AI Agent Platform Team"
LABEL description="Agent Orchestrator - AI reasoning engine for AI Agent Platform"

# Create app directory
WORKDIR /app

# Copy Maven wrapper and pom files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY backend/pom.xml ./backend/
COPY backend/common/pom.xml ./backend/common/
COPY backend/agent-orchestrator/pom.xml ./backend/agent-orchestrator/

# Download dependencies
RUN ./mvnw dependency:go-offline -DskipTests

# Copy source code
COPY backend/common/src ./backend/common/src
COPY backend/agent-orchestrator/src ./backend/agent-orchestrator/src

# Build the application
RUN ./mvnw clean package -DskipTests

# Create runtime directories
RUN useradd -m -u 1000 orchestratoruser && chown -R orchestratoruser:orchestratoruser /app

# Switch to non-root user
USER orchestratoruser

# Expose port
EXPOSE 8082

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8082/api/v1/actuator/health || exit 1

# Run the application
CMD ["java", "-jar", "backend/agent-orchestrator/target/agent-orchestrator-1.0-SNAPSHOT.jar"]
