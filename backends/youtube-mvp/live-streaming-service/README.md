# Live Streaming Service

Production-grade microservice for managing live streaming events with Azure Media Services integration, implementing hexagonal architecture, CQRS patterns, and comprehensive resilience features.

## Architecture

This service follows **Hexagonal Architecture** (Ports & Adapters) with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                    Interface Layer                          │
│  REST Controllers (LiveEventController, AmsCallbackController)│
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────────┐
│                   Application Layer                          │
│  Orchestration Services, DTOs, Mappers (MapStruct)          │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────────┐
│                     Domain Layer                             │
│  Entities (LiveEvent), Value Objects, Domain Events, Ports  │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────────┐
│                 Infrastructure Layer                         │
│  Persistence (JPA/PostgreSQL), External (AMS Adapter),      │
│  Messaging (Service Bus), Cache (Redis)                     │
└─────────────────────────────────────────────────────────────┘
```

## Features

- ✅ **Live Event Lifecycle Management**: Create, start, stop, archive live events
- ✅ **Azure Media Services Integration**: Full AMS Live Events API integration
- ✅ **State Machine**: Enforced state transitions for event lifecycle
- ✅ **Resilience Patterns**: Circuit breaker, retry, timeout, bulkhead
- ✅ **Idempotency**: Support for `Idempotency-Key` header with Redis deduplication
- ✅ **ETag Support**: Optimistic concurrency control
- ✅ **OpenAPI Documentation**: Swagger UI with comprehensive API docs
- ✅ **Azure Integration**: Cosmos DB, Service Bus, Key Vault, App Configuration
- ✅ **Observability**: OpenTelemetry, metrics, distributed tracing
- ✅ **Security**: OAuth2 Resource Server with Entra External ID/B2C

## Tech Stack

- **Java 17** with Spring Boot 3.3.x
- **PostgreSQL** for event persistence
- **Redis** for caching and idempotency
- **Azure Media Services** for live streaming
- **Azure Service Bus** for event publishing
- **Lombok** & **MapStruct** for boilerplate reduction
- **Resilience4j** for resilience patterns
- **Testcontainers** for integration tests

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL 15+
- Redis 7+

### Local Development

1. **Start dependencies** (PostgreSQL, Redis, Azurite):
```bash
make docker-up
```

2. **Run the application**:
```bash
make run-local
```

3. **Access the service**:
- API: http://localhost:8080/api/v1/live/events
- Swagger UI: http://localhost:8080/swagger-ui.html
- Actuator Health: http://localhost:8080/actuator/health

### Environment Variables

Key environment variables (configure in `application-local.yml`):

```yaml
AZURE_POSTGRESQL_URL: jdbc:postgresql://localhost:5432/livestreaming
AZURE_POSTGRESQL_USERNAME: postgres
AZURE_POSTGRESQL_PASSWORD: postgres
AZURE_REDIS_HOST: localhost
AZURE_REDIS_PORT: 6379

# Azure Media Services
AZURE_MEDIA_SERVICES_ACCOUNT_NAME: your-account
AZURE_MEDIA_SERVICES_RESOURCE_GROUP: your-rg
AZURE_SUBSCRIPTION_ID: your-sub-id
AZURE_CLIENT_ID: your-client-id
AZURE_CLIENT_SECRET: your-secret
AZURE_TENANT_ID: your-tenant-id
```

## API Endpoints

### Live Event Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/live/events` | Create a new live event |
| GET | `/api/v1/live/events/{id}` | Get live event by ID |
| POST | `/api/v1/live/events/{id}/start` | Start a live event |
| POST | `/api/v1/live/events/{id}/stop` | Stop a live event |
| GET | `/api/v1/live/events/channel/{channelId}` | Get events by channel |
| GET | `/api/v1/live/events/my-events` | Get user's events |

### AMS Callbacks

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/events/ams-live-callback` | Handle AMS state changes |

### Example: Create Live Event

```bash
curl -X POST http://localhost:8080/api/v1/live/events \
  -H "Authorization: Bearer <token>" \
  -H "Idempotency-Key: unique-key-123" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My Live Stream",
    "description": "Event description",
    "channelId": "channel-123",
    "dvr": {
      "enabled": true,
      "windowInMinutes": 120
    },
    "lowLatencyEnabled": true
  }'
```

### Example: Response

```json
{
  "id": "live-abc123",
  "userId": "user-456",
  "channelId": "channel-123",
  "name": "My Live Stream",
  "state": "CREATED",
  "ingestUrl": "rtmp://account.ingest.azure.net/live/live-abc123",
  "previewUrl": "https://account.streaming.mediaservices.windows.net/live-abc123/Manifest",
  "createdAt": "2024-01-15T10:00:00Z",
  "isRunning": false
}
```

## Resilience Configuration

The service uses **Resilience4j** for fault tolerance:

### Circuit Breaker
```yaml
resilience4j:
  circuitbreaker:
    instances:
      ams-client:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 5s
```

### Retry
```yaml
resilience4j:
  retry:
    instances:
      ams-client:
        maxAttempts: 3
        waitDuration: 1s
        exponentialBackoffMultiplier: 2
```

### Time Limiter
```yaml
resilience4j:
  timelimiter:
    instances:
      ams-client:
        timeoutDuration: 10s
```

## Idempotency

The service supports idempotent requests via the `Idempotency-Key` header:

```bash
curl -X POST http://localhost:8080/api/v1/live/events \
  -H "Idempotency-Key: unique-id-12345"
```

- First request: Creates new live event, returns 201
- Subsequent requests with same key: Returns existing event, 200

Idempotency keys are stored in Redis with 24-hour TTL.

## Docker

### Build
```bash
make docker-build
```

### Run with Docker Compose
```bash
docker-compose up -d
```

## Kubernetes Deployment

Deploy to Kubernetes:

```bash
# Create namespace and secrets first
kubectl create namespace live-streaming
kubectl create secret generic live-streaming-secrets \
  --from-literal=postgres-url=$POSTGRES_URL \
  --from-literal=ams-account-name=$AMS_ACCOUNT

# Apply manifests
make k8s-deploy

# Check status
kubectl get pods -n live-streaming
```

### Manifests

- `deployment.yaml`: Deployment with health probes
- `service.yaml`: ClusterIP service
- `hpa.yaml`: Horizontal Pod Autoscaler (3-20 replicas)
- `pdb.yaml`: Pod Disruption Budget (min 2 available)
- `network-policy.yaml`: Network isolation

## Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn verify
```

Integration tests use **Testcontainers** for PostgreSQL and Redis.

## Development

### Project Structure

```
live-streaming-service/
├── src/main/java/com/youtube/livestreaming/
│   ├── domain/              # Domain layer
│   │   ├── entities/        # LiveEvent aggregate
│   │   ├── valueobjects/    # Value objects
│   │   ├── ports/           # Repository & service ports
│   │   └── events/          # Domain events
│   ├── application/          # Application layer
│   │   ├── services/        # Orchestration services
│   │   ├── dtos/            # Data Transfer Objects
│   │   └── mappers/         # MapStruct mappers
│   ├── infrastructure/      # Infrastructure layer
│   │   ├── adapters/        # External adapters
│   │   │   ├── external/   # AMS client adapter
│   │   │   ├── persistence/ # Redis idempotency
│   │   │   └── messaging/  # Service Bus publisher
│   │   ├── persistence/     # JPA entities & repositories
│   │   └── config/          # Spring configurations
│   ├── interfaces/          # Interface layer
│   │   └── rest/            # REST controllers
│   └── shared/              # Shared utilities
├── src/main/resources/
│   ├── application.yml      # Main config
│   ├── application-local.yml
│   └── db/migration/        # Flyway migrations
├── src/test/                # Tests
└── k8s/                     # Kubernetes manifests
```

## Monitoring & Observability

### Health Checks

- **Liveness**: `/actuator/health/liveness`
- **Readiness**: `/actuator/health/readiness`
- **Metrics**: `/actuator/metrics`
- **Prometheus**: `/actuator/prometheus`

### OpenTelemetry

The service exports traces and metrics to Azure Monitor via OpenTelemetry.

### Correlation IDs

Every request includes a correlation ID in headers for distributed tracing.

## Azure APIM Policy Snippets

### JWT Validation & Rate Limiting

```xml
<policies>
  <inbound>
    <!-- Validate JWT -->
    <validate-jwt header-name="Authorization" failed-validation-httpcode="401">
      <openid-config url="https://your-tenant.b2clogin.com/your-tenant/your-policy/v2.0/.well-known/openid-configuration" />
      <audiences>
        <audience>your-client-id</audience>
      </audiences>
    </validate-jwt>
    
    <!-- Rate limiting -->
    <rate-limit-by-key calls="100" renewal-period="60" counter-key="@(context.Request.IpAddress)" />
    
    <base />
  </inbound>
  <backend>
    <base />
  </backend>
  <outbound>
    <base />
  </outbound>
</policies>
```

## License

Copyright © 2024 YouTube MVP

