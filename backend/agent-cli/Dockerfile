FROM eclipse-temurin:21-jdk-alpine AS builder

LABEL maintainer="AI Agent Platform Team"
LABEL description="Agent CLI - Command-line interface for AI Agent Platform"

WORKDIR /build

# Install Maven
RUN apk add --no-cache maven

# Copy pom files
COPY pom.xml .
COPY backend/pom.xml ./backend/
COPY backend/common/pom.xml ./backend/common/
COPY backend/mcp-server/pom.xml ./backend/mcp-server/
COPY backend/agent-orchestrator/pom.xml ./backend/agent-orchestrator/
COPY backend/agent-cli/pom.xml ./backend/agent-cli/

# Download dependencies
RUN mvn dependency:go-offline -DskipTests

# Copy source code
COPY backend/common/src ./backend/common/src
COPY backend/agent-orchestrator/src ./backend/agent-orchestrator/src
COPY backend/agent-cli/src ./backend/agent-cli/src

# Build the application
RUN mvn clean package -pl backend/agent-cli -am -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /build/backend/agent-cli/target/agent-cli-*.jar app.jar

# Create non-root user
RUN adduser -D -u 1000 cliuser && chown -R cliuser:cliuser /app

USER cliuser

# Set entrypoint
ENTRYPOINT ["java", "-jar", "app.jar"]

# Default command (shows help)
CMD ["run", "help"]
