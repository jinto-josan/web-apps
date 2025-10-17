#!/bin/bash

# Clean Architecture Folder Structure Generator
# This script creates Clean Architecture folder structure for microservices in any project
# 
# Usage:
#   ./create-clean-architecture.sh [project-root] [base-package] [services...]
#
# Examples:
#   ./create-clean-architecture.sh . com.youtube identity-auth-service user-profile-service
#   ./create-clean-architecture.sh /path/to/project com.company service1 service2 service3
#
# If no arguments provided, it will prompt for input

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Function to show usage
show_usage() {
    echo "Clean Architecture Folder Structure Generator"
    echo ""
    echo "Usage:"
    echo "  $0 [project-root] [base-package] [services...]"
    echo ""
    echo "Arguments:"
    echo "  project-root    : Root directory of the project (default: current directory)"
    echo "  base-package     : Base package name (e.g., com.youtube, com.company)"
    echo "  services        : Space-separated list of service names"
    echo ""
    echo "Examples:"
    echo "  $0 . com.youtube identity-auth-service user-profile-service"
    echo "  $0 /path/to/project com.company service1 service2 service3"
    echo "  $0  # Interactive mode - will prompt for input"
    echo ""
    echo "Service names should match existing directories in the project root."
}

# Function to get user input
get_user_input() {
    if [ $# -eq 0 ]; then
        echo ""
        print_info "Interactive mode - please provide the following information:"
        echo ""
        
        read -p "Project root directory (default: current directory): " PROJECT_ROOT
        PROJECT_ROOT=${PROJECT_ROOT:-.}
        
        read -p "Base package name (e.g., com.youtube): " BASE_PACKAGE
        if [ -z "$BASE_PACKAGE" ]; then
            print_error "Base package name is required!"
            exit 1
        fi
        
        echo ""
        print_info "Available directories in $PROJECT_ROOT:"
        ls -1 "$PROJECT_ROOT" | grep -E '^[a-zA-Z0-9_-]+$' | head -10
        echo ""
        
        read -p "Enter service names (space-separated): " SERVICES_INPUT
        if [ -z "$SERVICES_INPUT" ]; then
            print_error "At least one service name is required!"
            exit 1
        fi
        
        SERVICES=($SERVICES_INPUT)
    else
        PROJECT_ROOT=${1:-.}
        BASE_PACKAGE=$2
        shift 2
        SERVICES=("$@")
    fi
}

# Function to validate inputs
validate_inputs() {
    if [ ! -d "$PROJECT_ROOT" ]; then
        print_error "Project root directory '$PROJECT_ROOT' does not exist!"
        exit 1
    fi
    
    if [ -z "$BASE_PACKAGE" ]; then
        print_error "Base package name is required!"
        exit 1
    fi
    
    if [ ${#SERVICES[@]} -eq 0 ]; then
        print_error "At least one service name is required!"
        exit 1
    fi
}

# Function to create Clean Architecture structure for a service
create_service_structure() {
    local service_name=$1
    local service_dir="$PROJECT_ROOT/$service_name"
    local package_name=$(echo "$service_name" | sed 's/-//g')
    local full_package="$BASE_PACKAGE.$package_name"
    
    print_info "Creating Clean Architecture structure for $service_name..."
    
    # Check if service directory exists
    if [ ! -d "$service_dir" ]; then
        print_warning "Service directory $service_name not found, skipping..."
        return 1
    fi
    
    # Remove existing src directory if it exists
    if [ -d "$service_dir/src" ]; then
        print_info "Removing existing src directory..."
        rm -rf "$service_dir/src"
    fi
    
    # Create main source directories
    mkdir -p "$service_dir/src/main/java/$(echo $full_package | tr '.' '/')/domain/entities"
    mkdir -p "$service_dir/src/main/java/$(echo $full_package | tr '.' '/')/domain/valueobjects"
    mkdir -p "$service_dir/src/main/java/$(echo $full_package | tr '.' '/')/domain/repositories"
    mkdir -p "$service_dir/src/main/java/$(echo $full_package | tr '.' '/')/domain/services"
    mkdir -p "$service_dir/src/main/java/$(echo $full_package | tr '.' '/')/domain/events"
    
    mkdir -p "$service_dir/src/main/java/$(echo $full_package | tr '.' '/')/application/usecases"
    mkdir -p "$service_dir/src/main/java/$(echo $full_package | tr '.' '/')/application/commands"
    mkdir -p "$service_dir/src/main/java/$(echo $full_package | tr '.' '/')/application/queries"
    mkdir -p "$service_dir/src/main/java/$(echo $full_package | tr '.' '/')/application/handlers"
    mkdir -p "$service_dir/src/main/java/$(echo $full_package | tr '.' '/')/application/services"
    
    mkdir -p "$service_dir/src/main/java/$(echo $full_package | tr '.' '/')/infrastructure/persistence"
    mkdir -p "$service_dir/src/main/java/$(echo $full_package | tr '.' '/')/infrastructure/external"
    mkdir -p "$service_dir/src/main/java/$(echo $full_package | tr '.' '/')/infrastructure/config"
    mkdir -p "$service_dir/src/main/java/$(echo $full_package | tr '.' '/')/infrastructure/messaging"
    
    mkdir -p "$service_dir/src/main/java/$(echo $full_package | tr '.' '/')/interfaces/rest"
    mkdir -p "$service_dir/src/main/java/$(echo $full_package | tr '.' '/')/interfaces/graphql"
    mkdir -p "$service_dir/src/main/java/$(echo $full_package | tr '.' '/')/interfaces/events"
    
    mkdir -p "$service_dir/src/main/java/$(echo $full_package | tr '.' '/')/shared/exceptions"
    mkdir -p "$service_dir/src/main/java/$(echo $full_package | tr '.' '/')/shared/utils"
    mkdir -p "$service_dir/src/main/java/$(echo $full_package | tr '.' '/')/shared/constants"
    
    # Create resources directories
    mkdir -p "$service_dir/src/main/resources/db/migration"
    mkdir -p "$service_dir/src/main/resources/config"
    mkdir -p "$service_dir/src/main/resources/templates"
    
    # Create test directories
    mkdir -p "$service_dir/src/test/java/$(echo $full_package | tr '.' '/')/domain"
    mkdir -p "$service_dir/src/test/java/$(echo $full_package | tr '.' '/')/application"
    mkdir -p "$service_dir/src/test/java/$(echo $full_package | tr '.' '/')/infrastructure"
    mkdir -p "$service_dir/src/test/java/$(echo $full_package | tr '.' '/')/interfaces"
    mkdir -p "$service_dir/src/test/resources"
    
    print_success "Created structure for $service_name (package: $full_package)"
}

# Function to create example migration file
create_example_migration() {
    local service_name=$1
    local service_dir="$PROJECT_ROOT/$service_name"
    local migration_file="$service_dir/src/main/resources/db/migration/V1__Create_${service_name}_tables.sql"
    
    if [ ! -f "$migration_file" ]; then
        cat > "$migration_file" << EOF
-- $service_name Database Schema
-- Migration: V1__Create_${service_name}_tables.sql

-- Add your database schema here
-- Example:
-- CREATE TABLE ${service_name}_entities (
--     id VARCHAR(26) PRIMARY KEY,
--     name VARCHAR(255) NOT NULL,
--     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
--     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
-- );

-- CREATE INDEX idx_${service_name}_entities_name ON ${service_name}_entities(name);
EOF
        print_info "Created example migration file: $migration_file"
    fi
}

# Function to create README for the service
create_service_readme() {
    local service_name=$1
    local service_dir="$PROJECT_ROOT/$service_name"
    local readme_file="$service_dir/README.md"
    
    if [ ! -f "$readme_file" ]; then
        cat > "$readme_file" << EOF
# $service_name

This service follows Clean Architecture principles.

## Structure

\`\`\`
src/main/java/$BASE_PACKAGE.$(echo $service_name | sed 's/-//g')/
├── domain/                    # Domain Layer
│   ├── entities/             # Domain entities
│   ├── valueobjects/         # Value objects
│   ├── repositories/         # Repository interfaces
│   ├── services/             # Domain services
│   └── events/               # Domain events
├── application/              # Application Layer
│   ├── usecases/             # Use case implementations
│   ├── commands/             # Command objects (CQRS)
│   ├── queries/              # Query objects (CQRS)
│   ├── handlers/             # Command/Query handlers
│   └── services/             # Application services
├── infrastructure/           # Infrastructure Layer
│   ├── persistence/          # Database implementations
│   ├── external/             # External service clients
│   ├── config/               # Configuration classes
│   └── messaging/            # Message handling
├── interfaces/               # Interface Layer
│   ├── rest/                 # REST controllers
│   ├── graphql/              # GraphQL resolvers
│   └── events/               # Event listeners
└── shared/                   # Shared utilities
    ├── exceptions/           # Custom exceptions
    ├── utils/                # Utility classes
    └── constants/            # Constants
\`\`\`

## Database Migrations

Database migrations are located in \`src/main/resources/db/migration/\`.

Migration files should follow the naming convention: \`V{version}__{description}.sql\`

## Getting Started

1. Add your domain entities in \`domain/entities/\`
2. Define repository interfaces in \`domain/repositories/\`
3. Implement use cases in \`application/usecases/\`
4. Create REST controllers in \`interfaces/rest/\`
5. Implement persistence in \`infrastructure/persistence/\`
EOF
        print_info "Created README file: $readme_file"
    fi
}

# Main execution
main() {
    echo "🏗️  Clean Architecture Folder Structure Generator"
    echo "=================================================="
    
    # Get user input
    get_user_input "$@"
    
    # Validate inputs
    validate_inputs
    
    # Show configuration
    echo ""
    print_info "Configuration:"
    echo "  Project Root: $PROJECT_ROOT"
    echo "  Base Package: $BASE_PACKAGE"
    echo "  Services: ${SERVICES[*]}"
    echo ""
    
    # Create structure for all services
    local success_count=0
    local total_count=${#SERVICES[@]}
    
    for service in "${SERVICES[@]}"; do
        if create_service_structure "$service"; then
            create_example_migration "$service"
            create_service_readme "$service"
            ((success_count++))
        fi
    done
    
    echo ""
    echo "🎉 Clean Architecture structure generation completed!"
    echo "   Successfully processed: $success_count/$total_count services"
    echo ""
    
    if [ $success_count -gt 0 ]; then
        print_info "Next steps:"
        echo "  1. Review the generated folder structure"
        echo "  2. Update migration files in src/main/resources/db/migration/"
        echo "  3. Start implementing your domain entities"
        echo "  4. Add your use cases and application services"
        echo "  5. Implement REST controllers and infrastructure"
        echo ""
        print_info "For detailed documentation, see: utilities/clean-architecture/README.md"
    fi
}

# Handle help flag
if [[ "$1" == "-h" || "$1" == "--help" ]]; then
    show_usage
    exit 0
fi

# Run main function
main "$@"
