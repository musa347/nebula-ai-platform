FROM openjdk:21-jdk-slim

LABEL maintainer="AI Agent Platform Team"
LABEL description="MCP Server - Tool execution runtime for AI Agent Platform"

# Create app directory
WORKDIR /app

# Copy Maven wrapper and pom files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY backend/pom.xml ./backend/
COPY backend/common/pom.xml ./backend/common/
COPY backend/mcp-server/pom.xml ./backend/mcp-server/

# Download dependencies
RUN ./mvnw dependency:go-offline -DskipTests

# Copy source code
COPY backend/common/src ./backend/common/src
COPY backend/mcp-server/src ./backend/mcp-server/src

# Build the application
RUN ./mvnw clean package -DskipTests

# Create runtime directories
RUN mkdir -p /workspace /tmp/mcp-sandbox
RUN useradd -m -u 1000 mcpuser && chown -R mcpuser:mcpuser /app /workspace /tmp/mcp-sandbox

# Switch to non-root user
USER mcpuser

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/api/v1/actuator/health || exit 1

# Run the application
CMD ["java", "-jar", "backend/mcp-server/target/mcp-server-1.0-SNAPSHOT.jar"]
