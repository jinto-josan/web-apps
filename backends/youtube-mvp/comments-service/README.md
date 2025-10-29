# Comments and Community Service

A production-grade microservice for managing comments, replies, and reactions on video content with real-time broadcasting capabilities.

## Architecture

- **Hexagonal Architecture** (Ports & Adapters)
- **Domain-Driven Design** (DDD-lite)
- **CQRS** for read/write separation
- **Event-Driven** with Azure Service Bus
- **Real-time Updates** via Azure Web PubSub

## Technology Stack

- Java 17
- Spring Boot 3.2+
- Azure Cosmos DB (NoSQL)
- Azure Redis Cache
- Azure Service Bus
- Azure Web PubSub
- Resilience4j (Retry, Circuit Breaker, Bulkhead, Rate Limiter)

## Features

- ✅ Idempotent comment creation
- ✅ Flood control with Redis rate limiting
- ✅ Profanity filtering
- ✅ Comment reactions (likes, etc.)
- ✅ Nested replies
- ✅ Real-time broadcasting
- ✅ Outbox pattern for reliable event publishing
- ✅ ProblemDetails (RFC 7807) error handling
- ✅ OpenAPI/Swagger documentation
- ✅ Comprehensive observability

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.9+
- Docker & Docker Compose (for local dependencies)
- Azure Account (for cloud deployment)

### Local Development

1. Start local dependencies:
```bash
make local-up
# or
docker-compose up -d
```

2. Run the service:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

3. Access Swagger UI:
```
http://localhost:8080/swagger-ui.html
```

### Build

```bash
mvn clean package
```

### Docker

```bash
# Build
make docker-build

# Run
make docker-run
```

### Kubernetes Deployment

```bash
# Apply K8s manifests
kubectl apply -f k8s/

# Check deployment status
kubectl get deployment comments-service
```

## API Endpoints

Base URL: `/api/v1/videos/{videoId}/comments`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/videos/{videoId}/comments` | Create a comment |
| GET | `/api/v1/videos/{videoId}/comments` | List comments |
| DELETE | `/api/v1/videos/{videoId}/comments/{commentId}` | Delete a comment |
| POST | `/api/v1/videos/{videoId}/comments/{commentId}/reactions` | Add reaction |
| DELETE | `/api/v1/videos/{videoId}/comments/{commentId}/reactions/{type}` | Remove reaction |

### Authentication

All endpoints require JWT authentication via OAuth2 Resource Server. Include the access token in the `Authorization` header:

```
Authorization: Bearer <your-token>
```

### Idempotency

Use the `Idempotency-Key` header to ensure idempotent requests:

```
Idempotency-Key: <unique-key>
```

## Environment Variables

### Required

| Variable | Description |
|----------|-------------|
| `AZURE_TENANT_ID` | Azure AD tenant ID |
| `AZURE_CLIENT_ID` | Azure AD client ID |
| `AZURE_CLIENT_SECRET` | Azure AD client secret |
| `AZURE_COSMOS_ENDPOINT` | Cosmos DB endpoint |
| `AZURE_COSMOS_KEY` | Cosmos DB key |
| `AZURE_SERVICEBUS_CONNECTION_STRING` | Service Bus connection string |
| `AZURE_WEBPUBSUB_CONNECTION_STRING` | Web PubSub connection string |
| `REDIS_HOST` | Redis host |
| `REDIS_PORT` | Redis port |

### Optional

| Variable | Description | Default |
|----------|-------------|---------|
| `AZURE_COSMOS_DATABASE` | Cosmos DB database name | `comments` |
| `SPRING_PROFILES_ACTIVE` | Active profile | `default` |

## Architecture Components

### Domain Layer

- `Comment` - Aggregate root
- `ReactionCount` - Value object
- `CommentRepository` - Repository interface
- Domain events: `CommentCreatedEvent`, `CommentDeletedEvent`

### Application Layer

- `CommentApplicationService` - Application services
- `CreateCommentCommand` / `AddReactionCommand` - Commands
- `GetCommentsQuery` - Queries
- DTOs and Mappers

### Infrastructure Layer

- `CosmosCommentRepository` - Cosmos DB adapter
- `RedisIdempotencyCheckerAdapter` - Redis adapter
- `ServiceBusEventPublisherAdapter` - Service Bus adapter
- `WebPubSubBroadcastAdapter` - Web PubSub adapter

## Resilience Patterns

### Circuit Breaker
- Prevents cascading failures
- Configuration: `resilience4j.circuitbreaker`

### Retry
- Automatic retry on transient failures
- Configuration: `resilience4j.retry`

### Rate Limiter
- Prevents API abuse
- Configuration: `resilience4j.ratelimiter`

### Bulkhead
- Isolates thread pools
- Configuration: `resilience4j.bulkhead`

## Observability

### Metrics
- Prometheus endpoint: `/actuator/prometheus`
- Azure Monitor integration enabled

### Logs
- Structured JSON logs
- Correlation ID propagation

### Traces
- OpenTelemetry integration
- Azure Monitor exporter

## Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
Requires Testcontainers for Cosmos DB and Redis emulators.

### Load Testing
See `docs/load-testing.md` for JMeter/Gatling examples.

## API Management (Azure APIM)

### JWT Validation Policy
```xml
<policies>
  <inbound>
    <validate-jwt header-name="Authorization" failed-validation-httpcode="401" failed-validation-error-message="Unauthorized" require-expiration-time="true" require-signed-tokens="true">
      <openid-config config-name="entra-id" url="https://login.microsoftonline.com/{tenant-id}/.well-known/openid_configuration" />
    </validate-jwt>
  </inbound>
</policies>
```

### Rate Limiting Policy
```xml
<policies>
  <inbound>
    <rate-limit-by-key calls="100" renewal-period="60" counter-key="@(context.Request.IpAddress)" />
  </inbound>
</policies>
```

## Monitoring & Alerts

### Key Metrics
- Request rate
- Error rate
- Response time (p50, p95, p99)
- Active connections
- Circuit breaker state

### Recommended Alerts
- Error rate > 5%
- P95 latency > 500ms
- Circuit breaker open > 1 minute
- Pod restarts > 3 in 5 minutes

## Scaling

### Horizontal Scaling
- HPA configured for CPU (70%) and Memory (80%)
- Min: 3 replicas, Max: 20 replicas

### Vertical Scaling
- Request: 250m CPU, 256Mi memory
- Limit: 500m CPU, 512Mi memory

## Database Schema

### Cosmos DB Container
- **Partition Key**: `/videoId`
- **Indexing**: All properties indexed
- **Throughput**: 400 RU/s (auto-scale)

## Security

- OAuth2 Resource Server (JWT validation)
- Network policies enforced in K8s
- Secrets stored in Key Vault
- Pod security context (non-root user)

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

Copyright (c) 2024

