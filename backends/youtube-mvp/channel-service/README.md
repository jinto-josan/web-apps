# Channel Service

Production-grade microservice for managing YouTube channels and subscriptions using **Java 17**, **Spring Boot 3.3+**, **Cosmos DB**, **Redis**, and **Azure Service Bus**.

## Features

- **Channels CRUD** - Create, read, update channels
- **Subscription Management** - Subscribe/unsubscribe with idempotent operations
- **Anti-Supernode Strategy** - Shard subscribers by suffix to prevent hotspotting
- **CQRS** - Separate read/write models for optimal performance
- **Cache-Aside** - Redis caching with automatic invalidation
- **Idempotent Writes** - Redis-deduplication via `Idempotency-Key` header
- **Outbox Pattern** - Reliable event publishing to Service Bus
- **Resilience4j** - Circuit breaker, retry, rate limiter, bulkhead
- **Observability** - OpenTelemetry integration with Azure Monitor

## Non-Functional Requirements

- Subscribe: **P95 < 100ms**
- Read operations: **P95 < 80ms**
- Idempotency window: **24 hours**
- Cache TTL: **1 hour**

## Architecture

### Hexagonal Architecture

```
┌─────────────────────────────────────────────────────┐
│                   Interfaces                         │
│         REST Controllers | GraphQL APIs             │
└─────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────┐
│               Application Layer                      │
│      Commands | Queries | Use Cases | Handlers        │
└─────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────┐
│                  Domain Layer                        │
│    Entities | Value Objects | Repositories (Ports)   │
└─────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────┐
│              Infrastructure Layer                    │
│  Cosmos DB | Redis | Service Bus | Adapters         │
└─────────────────────────────────────────────────────┘
```

### Data Model

**Subscriptions** (Cosmos DB):
- Partition Key: `shardSuffix` (last 2 chars of userId)
- Document ID: ULID
- Anti-supernode sharding prevents hotspot on popular channels

**Read Models** (CQRS):
- `channel-subscription-stats` - Aggregated stats per channel
- Materialized views for fast reads

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose (for local emulators)
- Azure CLI (for cloud deployment)

### Local Development with Emulators

```bash
# Start emulators
docker-compose up -d

# Build
mvn clean package -DskipTests

# Run
java -jar target/channel-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=local
```

The service will start on `http://localhost:8080`.

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `AZURE_COSMOS_ENDPOINT` | Cosmos DB endpoint | - |
| `AZURE_COSMOS_KEY` | Cosmos DB key | - |
| `AZURE_COSMOS_DATABASE` | Database name | `youtube-mvp` |
| `AZURE_SERVICE_BUS_CONNECTION_STRING` | Service Bus connection | - |
| `REDIS_HOST` | Redis host | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `OAUTH2_ISSUER_URI` | OAuth2 issuer | - |
| `OAUTH2_JWK_SET_URI` | JWK set URI | - |

### Docker

```bash
# Build image
docker build -t channel-service:latest .

# Run with env file
docker run --env-file .env channel-service:latest
```

### Kubernetes

```bash
# Deploy
kubectl apply -f k8s/

# Check status
kubectl get pods -l app=channel-service
kubectl logs -f deployment/channel-service

# Access service
kubectl port-forward svc/channel-service 8080:8080
```

## API Documentation

### Endpoints

#### Subscribe to Channel
```http
POST /api/v1/channels/{channelId}/subscriptions
Idempotency-Key: {uuid}
Authorization: Bearer {jwt}
Content-Type: application/json

{
  "notifyOnUpload": true,
  "notifyOnLive": true,
  "notifyOnCommunityPost": true,
  "notifyOnShorts": true
}
```

#### Unsubscribe from Channel
```http
DELETE /api/v1/channels/{channelId}/subscriptions
Idempotency-Key: {uuid}
Authorization: Bearer {jwt}
```

#### Get User Subscriptions
```http
GET /api/v1/users/{userId}/subscriptions?offset=0&limit=20
Authorization: Bearer {jwt}
```

#### Get Channel Subscription Stats
```http
GET /api/v1/channels/{channelId}/subscription-stats
Authorization: Bearer {jwt}
```

### OpenAPI Documentation

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

## Testing

```bash
# Unit tests
mvn test

# Integration tests (requires emulators)
mvn verify

# Coverage report
mvn jacoco:report
open target/site/jacoco/index.html
```

### Testcontainers

Integration tests use Testcontainers for:
- Cosmos DB emulator
- Redis
- Service Bus emulator (Azurite)

## Monitoring & Observability

### Metrics (Prometheus)
- `http://localhost:8080/actuator/metrics`
- Custom: `subscriptions.count`, `subscription.latency`

### Health
- Liveness: `http://localhost:8080/actuator/health/liveness`
- Readiness: `http://localhost:8080/actuator/health/readiness`

### Traces
OpenTelemetry traces exported to Azure Monitor.

## Production Deployment

### Azure Container Apps

```bash
az containerapp create \
  --name channel-service \
  --resource-group youtube-mvp \
  --image <registry>/channel-service:latest \
  --min-replicas 3 \
  --max-replicas 10 \
  --cpu 1.0 \
  --memory 2.0Gi \
  --env-vars $(cat .env | grep -v '^#' | xargs)
```

### APIM Policies

#### JWT Validation
```xml
<validate-jwt header-name="Authorization" failed-validation-httpcode="401">
    <issuers>
        <issuer>${oauth2-issuer}</issuer>
    </issuers>
    <openid-config url="${oauth2-jwk-set-uri}" />
    <audiences>
        <audience>${api-audience}</audience>
    </audiences>
</validate-jwt>
```

#### Rate Limiting
```xml
<rate-limit-by-key calls="100" renewal-period="60" counter-key="@(context.Request.IpAddress)" />
```

## Makefile

```bash
# Development
make run-local          # Run with local emulators
make test              # Run tests
make build             # Build JAR

# Docker
make docker-build      # Build Docker image
make docker-run        # Run Docker container

# K8s
make k8s-deploy        # Deploy to Kubernetes
make k8s-logs          # View logs
```

## Sequence Diagrams

See `docs/sequences/` for detailed flow diagrams:
- Subscribe flow with fan-out
- Idempotency handling
- Anti-supernode strategy
- Outbox pattern

## License

MIT
