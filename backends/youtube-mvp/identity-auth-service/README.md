# identity-auth-service

This service follows Clean Architecture principles and handles authentication and authorization for the YouTube MVP platform.

## 📁 Project Structure

```
src/main/java/com.youtube.identityauthservice/
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
```

## 📊 Documentation

### Architecture Diagrams
- **LLD Diagram**: `lld.puml` - Low-level design class diagram
- **Generated Image**: `identity-auth-service-lld.png`

### Sequence Diagrams
- **Location**: `sequence-diagrams/` folder
- **Documentation**: `sequence-diagrams/SEQUENCE_DIAGRAMS.md`
- **Available Flows**:
  - AAD Token Exchange Flow
  - Local Login Flow
  - Refresh Token Flow
  - Device Code Flow
  - MFA Setup & Verification Flows
  - Logout Flow
  - JWKS Endpoint Flow

## 🗄️ Database Migrations

Database migrations are located in `src/main/resources/db/migration/`.

Migration files should follow the naming convention: `V{version}__{description}.sql`

## 🚀 Getting Started

1. Add your domain entities in `domain/entities/`
2. Define repository interfaces in `domain/repositories/`
3. Implement use cases in `application/usecases/`
4. Create REST controllers in `interfaces/rest/`
5. Implement persistence in `infrastructure/persistence/`

## 🔧 Generating Diagrams

```bash
# Generate LLD diagram
java -jar plantuml.jar -tpng lld.puml

# Generate all sequence diagrams
cd sequence-diagrams
java -jar plantuml.jar -tpng *.puml
```
