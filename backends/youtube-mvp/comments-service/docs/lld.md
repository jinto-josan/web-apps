# Comments Service - Low-Level Design

## Architecture Overview

The Comments Service follows Hexagonal Architecture (Ports & Adapters) with clear separation between Domain, Application, Infrastructure, and Interface layers.

```
┌─────────────────────────────────────────────────────────────┐
│                     Interface Layer                          │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  REST Controllers (REST API with API versioning)     │   │
│  │  - CommentController                                  │   │
│  │  - ExceptionHandler (RFC 7807)                       │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                           ↕
┌─────────────────────────────────────────────────────────────┐
│                    Application Layer                         │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  CommentApplicationService                            │   │
│  │  - Commands (CreateComment, DeleteComment)           │   │
│  │  - Queries (GetComments)                              │   │
│  │  - DTOs and Mappers                                   │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                           ↕
┌─────────────────────────────────────────────────────────────┐
│                      Domain Layer                            │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Aggregate: Comment                                  │   │
│  │  - Business logic                                     │   │
│  │  - Value Objects: ReactionCount                      │   │
│  │  - Domain Events: CommentCreatedEvent               │   │
│  │  - Ports (Interfaces)                                │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                           ↕
┌─────────────────────────────────────────────────────────────┐
│                  Infrastructure Layer                       │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Adapters                                             │   │
│  │  - CosmosCommentRepository (Persistence)             │   │
│  │  - RedisIdempotencyCheckerAdapter                    │   │
│  │  - ServiceBusEventPublisherAdapter (Events)         │   │
│  │  - WebPubSubBroadcastAdapter (Real-time)             │   │
│  │  - SimpleProfanityFilterAdapter                      │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

## Domain Model

### Comment Aggregate

```java
class Comment {
    - id: String
    - videoId: String (Partition Key)
    - authorId: String
    - parentId: String (null for top-level)
    - text: String
    - status: CommentStatus
    - reactions: Map<String, ReactionCount>
    - replyCount: int
    - totalReactionCount: int
    - etag: String (for optimistic locking)
    - createdAt: Instant
    - updatedAt: Instant
    - deletedAt: Instant
    
    + create(videoId, authorId, parentId, text): Comment
    + addReaction(type, userId): void
    + removeReaction(type, userId): void
    + delete(): void
    + isReply(): boolean
}
```

### Value Objects

```java
class ReactionCount {
    - type: String
    - count: int
    - userReactions: Map<String, String>
    
    + addUserReaction(userId): void
    + removeUserReaction(userId): void
}
```

### Domain Events

```java
class CommentCreatedEvent {
    - commentId: String
    - videoId: String
    - authorId: String
    - parentId: String
    - text: String
    - timestamp: Instant
}

class CommentDeletedEvent {
    - commentId: String
    - videoId: String
    - authorId: String
    - timestamp: Instant
}
```

## Ports (Interfaces)

### Repository Ports
- `CommentRepository` - Domain repository interface

### Service Ports
- `ProfanityFilterPort` - Profanity filtering
- `IdempotencyCheckerPort` - Idempotency checking
- `EventPublisherPort` - Domain event publishing
- `BroadcastPort` - Real-time broadcasting

## Adapters

### Persistence Adapter
- `CosmosCommentRepository` - Implements CommentRepository using Azure Cosmos DB
- Uses partition key: `/videoId`

### External Service Adapters
- `RedisIdempotencyCheckerAdapter` - Redis for idempotency keys
- `SimpleProfanityFilterAdapter` - Profanity filtering (production: Azure Content Moderator)
- `ServiceBusEventPublisherAdapter` - Azure Service Bus for event publishing
- `WebPubSubBroadcastAdapter` - Azure Web PubSub for real-time broadcasting

## Data Flow

### Create Comment Flow

1. **Controller** receives POST request with Idempotency-Key
2. **Application Service** checks idempotency in Redis
3. **Profanity Filter** validates/filters text
4. **Domain Entity** creates Comment with business rules
5. **Repository** saves to Cosmos DB (partitioned by videoId)
6. **Event Publisher** publishes CommentCreatedEvent to Service Bus
7. **Broadcast Adapter** broadcasts to Web PubSub for real-time updates

### Read Flow

1. **Controller** receives GET request with pagination
2. **Application Service** queries Cosmos DB
3. Pagination: 20 comments per page (default)
4. Returns Page<CommentDto>

## Resilience Patterns

### Circuit Breaker
Applied to:
- Cosmos DB calls (cosmosRepository)
- Service Bus publishing (serviceBusPublisher)

Configuration:
- Sliding window: 10
- Failure threshold: 50%
- Half-open state calls: 3

### Retry
- Max attempts: 3
- Exponential backoff
- Applied to all external service calls

### Rate Limiter
- Limit: 10 requests/60s per user
- Prevents spam/flooding

### Bulkhead
- Max concurrent calls: 25
- Isolation for event publishing

## Data Storage

### Cosmos DB
- **Container**: `comments`
- **Partition Key**: `/videoId`
- **Throughput**: 400 RU/s (auto-scale enabled)
- **Indexing**: All fields indexed

Document Schema:
```json
{
  "id": "comment-123",
  "videoId": "video-456",
  "authorId": "user-789",
  "parentId": "comment-111", // null for top-level
  "text": "Great video!",
  "status": "ACTIVE",
  "reactions": {
    "like": {
      "type": "like",
      "count": 42,
      "userReactions": {
        "user1": "2024-01-01T00:00:00Z",
        "user2": "2024-01-01T00:01:00Z"
      }
    }
  },
  "replyCount": 5,
  "totalReactionCount": 42,
  "etag": "abc123",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

### Redis
- **Purpose**: Idempotency keys, hot thread caching
- **TTL**: 1 hour for idempotency keys
- **Key format**: `idempotency:{key}`

## API Design

### Endpoints
- `POST /api/v1/videos/{videoId}/comments` - Create comment
- `GET /api/v1/videos/{videoId}/comments` - List comments
- `DELETE /api/v1/videos/{videoId}/comments/{commentId}` - Delete comment
- `POST /api/v1/videos/{videoId}/comments/{commentId}/reactions` - Add reaction
- `DELETE /api/v1/videos/{videoId}/comments/{commentId}/reactions/{type}` - Remove reaction

### Authentication
- OAuth2 Resource Server (JWT)
- Required for all endpoints except health checks

### API Versioning
- Path-based versioning: `/api/v1/`
- Future versions: `/api/v2/`

## Observability

### Metrics
- Request rate (requests/sec)
- Error rate (%)
- Response time (p50, p95, p99)
- Circuit breaker state

### Logs
- Structured JSON logs
- Correlation ID propagation
- Log levels: INFO (production), DEBUG (development)

### Traces
- OpenTelemetry instrumentation
- Distributed tracing via Azure Monitor
- Service map visualization

## Security

### Authentication & Authorization
- OAuth2 Resource Server with JWT validation
- Claims extraction for user context
- Author-based deletion (only author can delete)

### Network Security
- Network policies in K8s
- Pod security context (non-root user)
- Secrets in Key Vault

## Scalability

### Horizontal Scaling
- HPA: 3-20 replicas
- Based on CPU (70%) and Memory (80%)

### Database Scaling
- Cosmos DB auto-scale enabled
- Partition key: videoId (good distribution)

### Caching Strategy
- Redis cache for hot threads
- ETags for optimistic locking

## Error Handling

### RFC 7807 ProblemDetails
```json
{
  "type": "https://api.example.com/problems/validation-failed",
  "title": "Validation Failed",
  "status": 400,
  "detail": "text: must not be blank;",
  "instance": "/api/v1/videos/123/comments"
}
```

## Testing Strategy

### Unit Tests
- Application services
- Domain entities
- Value objects
- Mappers

### Integration Tests
- Testcontainers for Cosmos DB
- Testcontainers for Redis
- Mock Web PubSub

### Contract Tests
- WireMock for external services
- API contract validation

## Deployment

### Kubernetes
- Deployment with 3 replicas
- HPA for auto-scaling
- PodDisruptionBudget for HA
- Network policies for security
- Service with ClusterIP

### Helm Chart
- Template-based deployment
- Values files for environments
- Secrets management

## Configuration Management

### Azure App Configuration
- Feature flags
- Environment-specific settings

### Key Vault
- Secrets (OAuth2 credentials)
- Database credentials
- Connection strings

