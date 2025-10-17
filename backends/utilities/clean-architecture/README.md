# Clean Architecture Utilities

This directory contains utilities for setting up Clean Architecture folder structures in microservices projects.

## 📁 Contents

- **`create-clean-architecture.sh`** - Automated script to generate Clean Architecture folder structure
- **`README.md`** - This documentation file

## 🚀 Quick Start

### Basic Usage

```bash
# Make the script executable
chmod +x utilities/clean-architecture/create-clean-architecture.sh

# Run with specific parameters
./utilities/clean-architecture/create-clean-architecture.sh [project-root] [base-package] [services...]

# Interactive mode (will prompt for input)
./utilities/clean-architecture/create-clean-architecture.sh
```

### Examples

```bash
# For YouTube MVP project
./utilities/clean-architecture/create-clean-architecture.sh . com.youtube identity-auth-service user-profile-service channel-service

# For a different project
./utilities/clean-architecture/create-clean-architecture.sh /path/to/project com.company service1 service2 service3

# Interactive mode
./utilities/clean-architecture/create-clean-architecture.sh
```

## 📋 Parameters

| Parameter | Description | Example |
|-----------|-------------|---------|
| `project-root` | Root directory of the project | `.` (current directory) |
| `base-package` | Base package name for Java classes | `com.youtube`, `com.company` |
| `services` | Space-separated list of service names | `identity-auth-service user-profile-service` |

## 🏗️ Generated Structure

The script creates the following Clean Architecture folder structure for each service:

```
src/
├── main/
│   ├── java/{base-package}.{service}/
│   │   ├── domain/                    # Domain Layer (Business Logic)
│   │   │   ├── entities/             # Domain entities (aggregates)
│   │   │   ├── valueobjects/         # Value objects
│   │   │   ├── repositories/         # Repository interfaces
│   │   │   ├── services/             # Domain services
│   │   │   └── events/               # Domain events
│   │   ├── application/              # Application Layer (Use Cases)
│   │   │   ├── usecases/             # Use case implementations
│   │   │   ├── commands/             # Command objects (CQRS)
│   │   │   ├── queries/              # Query objects (CQRS)
│   │   │   ├── handlers/             # Command/Query handlers
│   │   │   └── services/             # Application services
│   │   ├── infrastructure/           # Infrastructure Layer (External Concerns)
│   │   │   ├── persistence/          # Database implementations
│   │   │   ├── external/             # External service clients
│   │   │   ├── config/               # Configuration classes
│   │   │   └── messaging/            # Message handling
│   │   ├── interfaces/               # Interface Layer (Adapters)
│   │   │   ├── rest/                 # REST controllers
│   │   │   ├── graphql/              # GraphQL resolvers
│   │   │   └── events/               # Event listeners
│   │   └── shared/                   # Shared utilities
│   │       ├── exceptions/           # Custom exceptions
│   │       ├── utils/                # Utility classes
│   │       └── constants/            # Constants
│   └── resources/
│       ├── db/migration/             # Flyway database migrations
│       ├── config/                   # Configuration files
│       └── templates/                # Template files
└── test/
    ├── java/{base-package}.{service}/ # Test classes (mirrors main structure)
    └── resources/                    # Test resources
```

## 🗄️ Database Migrations (Flyway)

Each service includes a `src/main/resources/db/migration/` folder for Flyway database migrations.

### Migration File Naming Convention
```
V{version}__{description}.sql
```

**Examples**:
- `V1__Create_users_table.sql`
- `V2__Add_user_indexes.sql`
- `V3__Create_sessions_table.sql`

### Migration Guidelines
1. **Sequential Versioning**: Use sequential numbers (V1, V2, V3...)
2. **Descriptive Names**: Use clear, descriptive names after the version
3. **Single Responsibility**: Each migration should have a single purpose
4. **Rollback Consideration**: Consider rollback scenarios when writing migrations
5. **Testing**: Test migrations in development before deploying

## 🏗️ Clean Architecture Layers

### 1. Domain Layer (`domain/`)
**Purpose**: Contains the core business logic and rules
- **Entities**: Core business objects with identity
- **Value Objects**: Immutable objects without identity
- **Repositories**: Interfaces for data access
- **Services**: Domain-specific business logic
- **Events**: Domain events for event-driven architecture

### 2. Application Layer (`application/`)
**Purpose**: Orchestrates use cases and coordinates between layers
- **Use Cases**: Application-specific business rules
- **Commands**: Write operations (CQRS pattern)
- **Queries**: Read operations (CQRS pattern)
- **Handlers**: Process commands and queries
- **Services**: Application-specific orchestration

### 3. Infrastructure Layer (`infrastructure/`)
**Purpose**: Implements external concerns and technical details
- **Persistence**: Database implementations of repository interfaces
- **External**: HTTP clients, third-party service integrations
- **Config**: Configuration classes and properties
- **Messaging**: Event publishing, message queues

### 4. Interface Layer (`interfaces/`)
**Purpose**: Handles external communication and adapts external requests
- **REST**: HTTP REST API controllers
- **GraphQL**: GraphQL resolvers and schema
- **Events**: Event listeners and handlers

### 5. Shared (`shared/`)
**Purpose**: Common utilities and cross-cutting concerns
- **Exceptions**: Custom exception classes
- **Utils**: Utility functions and helpers
- **Constants**: Application constants

## 🔄 Dependency Flow

Clean Architecture enforces dependency inversion:

```
Interfaces → Application → Domain
     ↓           ↓
Infrastructure → Application
```

- **Dependencies point inward**: Outer layers depend on inner layers
- **Domain is independent**: No dependencies on external frameworks
- **Abstractions**: Use interfaces to invert dependencies

## 🧪 Testing Structure

Test structure mirrors the main source structure:
- **Unit Tests**: Test individual components in isolation
- **Integration Tests**: Test component interactions
- **Contract Tests**: Test API contracts
- **End-to-End Tests**: Test complete user journeys

## 📝 Best Practices

1. **Single Responsibility**: Each class should have one reason to change
2. **Dependency Injection**: Use Spring's DI for loose coupling
3. **Interface Segregation**: Create focused interfaces
4. **Event-Driven**: Use domain events for loose coupling
5. **CQRS**: Separate read and write operations
6. **Immutable Value Objects**: Use immutable objects where possible
7. **Repository Pattern**: Abstract data access
8. **Factory Pattern**: Use factories for complex object creation

## 🚀 Getting Started

1. **Choose the appropriate layer** for your new class
2. **Follow naming conventions** for consistency
3. **Write tests** for your implementation
4. **Create migrations** for database changes
5. **Document** complex business logic

## 🔧 Script Features

- **Interactive Mode**: Prompts for input when no arguments provided
- **Validation**: Validates project root and service directories
- **Error Handling**: Graceful error handling with colored output
- **Example Files**: Creates example migration and README files
- **Progress Tracking**: Shows progress and success/failure counts
- **Flexible**: Works with any project structure and package naming

## 📋 Service Requirements

For the script to work properly, service directories should:
- Exist in the project root directory
- Have names that can be converted to valid Java package names
- Not have existing `src` directories (will be removed and recreated)

## 🎯 Use Cases

This utility is perfect for:
- **New Projects**: Setting up Clean Architecture from scratch
- **Existing Projects**: Converting existing services to Clean Architecture
- **Team Onboarding**: Ensuring consistent structure across teams
- **Code Reviews**: Maintaining architectural consistency
- **Documentation**: Providing clear structure documentation

## 🤝 Contributing

To improve this utility:
1. Fork the repository
2. Make your changes
3. Test with different project structures
4. Submit a pull request

## 📚 Additional Resources

- [Clean Architecture by Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Domain-Driven Design](https://martinfowler.com/bliki/DomainDrivenDesign.html)
- [CQRS Pattern](https://martinfowler.com/bliki/CQRS.html)
- [Flyway Documentation](https://flywaydb.org/documentation/)

This structure ensures maintainable, testable, and scalable microservices following Clean Architecture principles.
