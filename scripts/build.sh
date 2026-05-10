#!/bin/bash

# AI Agent Platform Build Script
# This script builds all modules in the correct order

set -e

echo " Building AI Agent Platform..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check prerequisites
check_prerequisites() {
    print_status "Checking prerequisites..."
    
    # Check Java version
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$JAVA_VERSION" -ge 21 ]; then
            print_status "✓ Java $JAVA_VERSION found"
        else
            print_error "Java 21 or higher is required. Found: $JAVA_VERSION"
            exit 1
        fi
    else
        print_error "Java is not installed"
        exit 1
    fi
    
    # Check Maven
    if command -v mvn &> /dev/null; then
        MVN_VERSION=$(mvn -version | head -n 1 | cut -d' ' -f3)
        print_status "✓ Maven $MVN_VERSION found"
    else
        print_error "Maven is not installed"
        exit 1
    fi
    
    # Check Gradle (for plugin)
    if command -v gradle &> /dev/null; then
        GRADLE_VERSION=$(gradle --version | grep "Gradle" | head -n 1 | cut -d' ' -f2)
        print_status "✓ Gradle $GRADLE_VERSION found"
    else
        print_warning "Gradle is not installed (required for IntelliJ plugin)"
    fi
}

# Build backend modules
build_backend() {
    print_status "Building backend modules..."
    
    # Store current directory and change to project root if needed
    local original_dir="$PWD"
    if [[ "$(basename $PWD)" == "scripts" ]]; then
        cd ..
    fi
    
    # Build common module first
    print_status "Building common module..."
    cd backend/common
    mvn clean install -DskipTests
    cd ../../
    
    # Build MCP server
    print_status "Building MCP server..."
    cd backend/mcp-server
    mvn clean install -DskipTests
    cd ../../
    
    # Build agent orchestrator
    print_status "Building agent orchestrator..."
    cd backend/agent-orchestrator
    mvn clean install -DskipTests
    cd ../../
    
    # Return to original directory
    cd "$original_dir"
    
    print_status "✓ Backend modules built successfully"
}

# Build IntelliJ plugin
build_plugin() {
    print_status "Building IntelliJ plugin..."
    
    # Store current directory and change to project root if needed
    local original_dir="$PWD"
    if [[ "$(basename $PWD)" == "scripts" ]]; then
        cd ..
    fi
    
    if command -v gradle &> /dev/null; then
        cd plugins/intellij-plugin
        ./gradlew buildPlugin
        cd ../..
        print_status "✓ IntelliJ plugin built successfully"
    else
        print_warning "Skipping plugin build (Gradle not available)"
    fi
    
    # Return to original directory
    cd "$original_dir"
}

# Run tests
run_tests() {
    if [ "$SKIP_TESTS" = "true" ]; then
        print_warning "Skipping tests (SKIP_TESTS=true)"
        return
    fi
    
    print_status "Running tests..."
    
    # Store current directory and change to project root if needed
    local original_dir="$PWD"
    if [[ "$(basename $PWD)" == "scripts" ]]; then
        cd ..
    fi
    
    # Test common module
    print_status "Testing common module..."
    cd backend/common
    mvn test
    cd ../../
    
    # Test MCP server
    print_status "Testing MCP server..."
    cd backend/mcp-server
    mvn test
    cd ../../
    
    # Test agent orchestrator
    print_status "Testing agent orchestrator..."
    cd backend/agent-orchestrator
    mvn test
    cd ../../
    
    # Return to original directory
    cd "$original_dir"
    
    print_status "✓ All tests passed"
}

# Build Docker images
build_docker() {
    if [ "$SKIP_DOCKER" = "true" ]; then
        print_warning "Skipping Docker build (SKIP_DOCKER=true)"
        return
    fi
    
    if command -v docker &> /dev/null; then
        print_status "Building Docker images..."
        
        # Store current directory and change to project root if needed
        local original_dir="$PWD"
        if [[ "$(basename $PWD)" == "scripts" ]]; then
            cd ..
        fi
        
        # Build MCP server image
        print_status "Building MCP server Docker image..."
        cd backend/mcp-server
        docker build -t ai-agent-platform/mcp-server:latest .
        cd ../../
        
        # Build agent orchestrator image
        print_status "Building agent orchestrator Docker image..."
        cd backend/agent-orchestrator
        docker build -t ai-agent-platform/agent-orchestrator:latest .
        cd ../../
        
        # Return to original directory
        cd "$original_dir"
        
        print_status "✓ Docker images built successfully"
    else
        print_warning "Skipping Docker build (Docker not available)"
    fi
}

# Main execution
main() {
    # Parse command line arguments
    SKIP_TESTS=false
    SKIP_DOCKER=false
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            --skip-tests)
                SKIP_TESTS=true
                shift
                ;;
            --skip-docker)
                SKIP_DOCKER=true
                shift
                ;;
            --help)
                echo "Usage: $0 [options]"
                echo "Options:"
                echo "  --skip-tests    Skip running tests"
                echo "  --skip-docker   Skip building Docker images"
                echo "  --help          Show this help message"
                exit 0
                ;;
            *)
                print_error "Unknown option: $1"
                exit 1
                ;;
        esac
    done
    
    # Execute build steps
    check_prerequisites
    build_backend
    build_plugin
    run_tests
    build_docker
    
    print_status " AI Agent Platform build completed successfully!"
    print_status ""
    print_status "Next steps:"
    print_status "1. Start MCP server: cd backend/mcp-server && mvn spring-boot:run"
    print_status "2. Start agent orchestrator: cd backend/agent-orchestrator && mvn spring-boot:run"
    print_status "3. Install IntelliJ plugin in IDEA from plugins/intellij-plugin/build/distributions/"
    print_status "4. Or use Docker: cd infrastructure/docker && docker-compose up"
}

# Run main function with all arguments
main "$@"
