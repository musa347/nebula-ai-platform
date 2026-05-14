FROM eclipse-temurin:21-jdk-alpine AS builder

LABEL maintainer="AI Agent Platform Team"
LABEL description="Agent Orchestrator - AI reasoning engine for AI Agent Platform"

WORKDIR /build

# Install Maven
RUN apk add --no-cache maven

# Copy pom files
COPY pom.xml .
COPY backend/pom.xml ./backend/
COPY backend/common/pom.xml ./backend/common/
COPY backend/agent-orchestrator/pom.xml ./backend/agent-orchestrator/

# Download dependencies
RUN mvn dependency:go-offline -DskipTests

# Copy source code
COPY backend/common/src ./backend/common/src
COPY backend/agent-orchestrator/src ./backend/agent-orchestrator/src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /build/backend/agent-orchestrator/target/agent-orchestrator-*.jar app.jar

# Create non-root user
RUN adduser -D -u 1000 orchestratoruser && chown -R orchestratoruser:orchestratoruser /app

USER orchestratoruser

EXPOSE 8082

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

CMD ["java", "-jar", "app.jar"]
