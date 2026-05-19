FROM eclipse-temurin:21-jdk-alpine AS builder

LABEL maintainer="AI Agent Platform Team"
LABEL description="Agent CLI - Command-line interface for AI Agent Platform"

WORKDIR /build

# Install Maven
RUN apk add --no-cache maven

# Copy everything needed for the build
COPY pom.xml .
COPY backend ./backend

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
