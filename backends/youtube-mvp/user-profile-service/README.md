# User Profile Service

This service manages user account profiles, privacy settings, notification preferences, and accessibility preferences for YouTube. It follows Clean Architecture principles with CQRS and Saga patterns.

## Architecture

This service follows Clean Architecture with clear separation of concerns across layers:

### Domain Layer (Core)
- **Entities**: AccountProfile, PrivacySettings, NotificationSettings, AccessibilityPreferences
- **Value Objects**: CaptionFontSize
- **Domain Events**: ProfileUpdated, PrivacySettingsChanged, NotificationPrefsChanged, AccessibilityPrefsChanged
- **Repository Interfaces**: ProfileRepository
- **Domain Services**: EventPublisher, CacheService, BlobUriValidator

### Application Layer
- **Commands**: UpdateProfileCommand, UpdatePrivacySettingsCommand, UpdateNotificationSettingsCommand, UpdateAccessibilityPreferencesCommand
- **Queries**: GetProfileQuery, GetPrivacySettingsQuery, GetNotificationSettingsQuery, GetAccessibilityPreferencesQuery
- **Sagas**: UpdateProfileSaga (with compensation logic)
- **Use Cases**: ProfileUseCase interface and implementation

### Infrastructure Layer
- **JPA Entities**: ProfileEntity, PrivacySettingsEmbeddable, NotificationSettingsEmbeddable, AccessibilityPreferencesEmbeddable
- **Repository Implementations**: ProfileRepositoryImpl (JPA)
- **Service Implementations**: EventPublisherImpl (Kafka), CacheServiceImpl (Redis)
- **Configuration**: SecurityConfig (OAuth2 JWT)

### Interface Layer
- **REST Controllers**: ProfileController
- **API Documentation**: OpenAPI/Swagger

## Key Features

### Profile Management
- Display name, locale, region, timezone
- Profile photo URL (validated against blob storage)
- Optimistic locking with ETags
- Audit trails (updatedBy, updatedAt)

### Privacy Settings
- Subscriptions privacy
- Saved playlists privacy
- Restricted mode
- Watch history privacy
- Like history privacy

### Notification Settings
- Email opt-in/opt-out
- Push notifications preferences
- Marketing communications
- Per-channel notification overrides
- Email and push notification preferences by type

### Accessibility Preferences
- Captions always on/off
- Captions language
- Autoplay default behavior
- Autoplay on home
- Caption font size
- Caption background opacity

## Design Patterns

### 1. Clean Architecture
- Clear layer separation
- Dependency inversion
- Framework independence
- High testability

### 2. CQRS (Command Query Responsibility Segregation)
- Separate commands for writes
- Separate queries for reads
- Optimized read and write models

### 3. Saga Pattern
- UpdateProfileSaga with compensation logic
- Step-by-step execution with rollback
- Event publishing and cache invalidation

### 4. Repository Pattern
- Data access abstraction
- Easy testing with mocks
- Clean persistence layer

## API Endpoints

### Profile Management
- `GET /profiles/{accountId}` - Get user profile
- `PATCH /profiles/{accountId}` - Update profile (with ETag support)

### Privacy Settings
- `GET /profiles/{accountId}/privacy` - Get privacy settings
- `PUT /profiles/{accountId}/privacy` - Update privacy settings

### Notification Settings
- `GET /profiles/{accountId}/notifications` - Get notification settings
- `PUT /profiles/{accountId}/notifications` - Update notification settings

## Authentication & Authorization

- **OAuth2 JWT** authentication required for all endpoints
- **Scopes Required**:
  - `profile.read` - Read operations
  - `profile.write` - Write operations

## Optimistic Locking

All update operations support optimistic locking via HTTP ETags:
- Client sends `If-Match: <etag>` header
- Server validates ETag matches current version
- Returns `409 Conflict` if ETag mismatch
- Returns `428 Precondition Required` if ETag missing

## Caching

- Redis cache for hot read paths
- Cache invalidation on updates
- TTL-based expiration
- Cache-aside pattern

## Events

The service publishes domain events:
- `ProfileUpdated` - When profile is updated
- `PrivacySettingsChanged` - When privacy settings change
- `NotificationPrefsChanged` - When notification preferences change
- `AccessibilityPrefsChanged` - When accessibility preferences change

## Database

- **PostgreSQL** for persistent storage
- **Flyway** for database migrations
- Partition by `account_id`
- Unique constraint on `account_id`

## Testing

Comprehensive test suite including:
- Unit tests for Sagas
- Integration tests with Testcontainers
- Mock-based testing for repositories
- Contract testing for APIs

## Getting Started

1. **Prerequisites**
   - Java 17+
   - Maven 3.6+
   - PostgreSQL 14+
   - Redis 6+
   - Kafka (for event publishing)

2. **Configuration**

   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/userprofiledb
       username: userprofileservice
       password: password
   
     redis:
       host: localhost
       port: 6379
   
   kafka:
     bootstrap-servers: localhost:9092
   ```

3. **Running the Service**
   ```bash
   mvn spring-boot:run
   ```

4. **API Documentation**
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - OpenAPI JSON: http://localhost:8080/v3/api-docs

## Project Structure

```
src/main/java/com/youtube/userprofileservice/
├── domain/                    # Domain Layer
│   ├── entities/              # Domain entities
│   ├── valueobjects/          # Value objects
│   ├── events/                # Domain events
│   ├── repositories/          # Repository interfaces
│   └── services/              # Domain services
├── application/               # Application Layer
│   ├── commands/              # Write commands (CQRS)
│   ├── queries/               # Read queries (CQRS)
│   ├── sagas/                 # Saga implementations
│   └── usecases/              # Use case interfaces
├── infrastructure/            # Infrastructure Layer
│   ├── config/                # Configuration
│   ├── persistence/           # JPA entities and repositories
│   ├── services/              # Service implementations
│   └── external/              # External service clients
└── interfaces/                # Interface Layer
    ├── rest/                  # REST controllers
    └── events/                # Event listeners
```

## Deployment

The service is designed for deployment on:
- **Azure Kubernetes Service (AKS)**
- **Cosmos DB** (alternative to PostgreSQL)
- **Azure Cache for Redis**
- **Azure Service Bus** (alternative to Kafka)

## License

Copyright © 2024 YouTube MVP
