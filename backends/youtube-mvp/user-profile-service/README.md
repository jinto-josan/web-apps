# user-profile-service

This service follows Clean Architecture principles.

## Structure

```
src/main/java/com.youtube.userprofileservice/
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

## Database Migrations

Database migrations are located in `src/main/resources/db/migration/`.

Migration files should follow the naming convention: `V{version}__{description}.sql`

## Getting Started

1. Add your domain entities in `domain/entities/`
2. Define repository interfaces in `domain/repositories/`
3. Implement use cases in `application/usecases/`
4. Create REST controllers in `interfaces/rest/`
5. Implement persistence in `infrastructure/persistence/`
