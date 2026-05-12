# CI/CD Pipeline

This project uses GitHub Actions for continuous integration and deployment.

## Workflows

### 1. Build and Test (`build.yml`)
**Triggers:** Push to any branch, Pull requests to master/main

**Jobs:**
- **build-backend**: Builds all backend modules (common, mcp-server, agent-orchestrator) and runs tests
- **build-plugin**: Builds and verifies the IntelliJ plugin
- **code-quality**: Runs static analysis tools (Checkstyle, SpotBugs)

**Artifacts:**
- Test results (JUnit XML)
- IntelliJ plugin ZIP

### 2. Docker Build and Push (`docker.yml`)
**Triggers:** Push to master/main, version tags (v*), manual dispatch

**Jobs:**
- Builds Docker images for mcp-server and agent-orchestrator
- Pushes images to GitHub Container Registry (ghcr.io)
- Tags images with branch name, SHA, and semantic version

**Image naming:**
```
ghcr.io/<username>/nebula-ai-platform/mcp-server:master
ghcr.io/<username>/nebula-ai-platform/agent-orchestrator:v1.0.0
```

### 3. Release (`release.yml`)
**Triggers:** Version tags (v*), manual dispatch

**Jobs:**
- **create-release**: Creates GitHub release with JAR files and plugin ZIP
- **publish-plugin**: Publishes plugin to JetBrains Marketplace (requires token)

### 4. Security Scan (`security.yml`)
**Triggers:** Weekly schedule, push to master/main, pull requests

**Jobs:**
- **dependency-check**: OWASP dependency vulnerability scanning
- **trivy-scan**: Container and filesystem security scanning

## Setup Requirements

### Secrets
Add these secrets in GitHub repository settings:

1. **JETBRAINS_MARKETPLACE_TOKEN** (optional)
   - Required for publishing plugin to JetBrains Marketplace
   - Get from: https://plugins.jetbrains.com/author/me/tokens

### Permissions
The workflows require these permissions:
- `contents: write` - for creating releases
- `packages: write` - for pushing Docker images to GHCR

## Local Testing

### Test Backend Build
```bash
mvn clean install -DskipTests
mvn test
```

### Test Plugin Build
```bash
cd plugins/intellij-plugin
./gradlew buildPlugin
./gradlew verifyPlugin
```

### Test Docker Build
```bash
# Build common first
mvn -pl backend/common clean install -DskipTests

# Build MCP Server image
cd backend/mcp-server
mvn clean package -DskipTests
docker build -t mcp-server:local .

# Build Agent Orchestrator image
cd ../agent-orchestrator
mvn clean package -DskipTests
docker build -t agent-orchestrator:local .
```

## Release Process

### Creating a Release

1. **Update version numbers** in:
   - `pom.xml` files
   - `plugins/intellij-plugin/build.gradle.kts`

2. **Commit and tag**:
   ```bash
   git add .
   git commit -m "chore: bump version to 1.0.0"
   git tag -a v1.0.0 -m "Release version 1.0.0"
   git push origin master --tags
   ```

3. **GitHub Actions will automatically**:
   - Build all artifacts
   - Create GitHub release
   - Push Docker images with version tag
   - Publish plugin to marketplace (if token configured)

### Manual Release
Trigger via GitHub UI:
1. Go to Actions → Release
2. Click "Run workflow"
3. Enter version number
4. Click "Run workflow"

## Monitoring

### Build Status
Check build status at:
```
https://github.com/<username>/nebula-ai-platform/actions
```

### Docker Images
View published images at:
```
https://github.com/<username>/nebula-ai-platform/pkgs/container/nebula-ai-platform%2Fmcp-server
https://github.com/<username>/nebula-ai-platform/pkgs/container/nebula-ai-platform%2Fagent-orchestrator
```

### Security Reports
View security scan results in:
- Actions → Security Scan → Latest run
- Security tab → Code scanning alerts

## Troubleshooting

### Build Fails on Tests
```bash
# Run tests locally to debug
mvn test -pl backend/mcp-server
```

### Docker Build Fails
```bash
# Check if JAR was built
ls -la backend/mcp-server/target/*.jar

# Build locally with verbose output
docker build --progress=plain -t test .
```

### Plugin Verification Fails
```bash
cd plugins/intellij-plugin
./gradlew verifyPlugin --stacktrace
```

## Future Enhancements

- [ ] Add deployment to Kubernetes
- [ ] Add integration tests in CI
- [ ] Add code coverage reporting (JaCoCo)
- [ ] Add performance benchmarks
- [ ] Add automated changelog generation
- [ ] Add Slack/Discord notifications
