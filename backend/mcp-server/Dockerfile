FROM eclipse-temurin:21-jdk-alpine AS builder

LABEL maintainer="AI Agent Platform Team"
LABEL description="MCP Server - Tool execution runtime for AI Agent Platform"

WORKDIR /build

# Install Maven
RUN apk add --no-cache maven

# Copy everything needed for the build
COPY pom.xml .
COPY backend ./backend

# Build the application
RUN mvn clean package -pl backend/mcp-server -am -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /build/backend/mcp-server/target/mcp-server-*.jar app.jar

# Create runtime directories and user
RUN mkdir -p /workspace /tmp/mcp-sandbox && \
    adduser -D -u 1000 mcpuser && \
    chown -R mcpuser:mcpuser /app /workspace /tmp/mcp-sandbox

USER mcpuser

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

CMD ["java", "-jar", "app.jar"]
