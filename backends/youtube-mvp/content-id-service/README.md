# Content ID Service

Production-grade microservice for content fingerprinting, matching, and claims/disputes management using **Java 17**, **Spring Boot 3.3.x**, **PostgreSQL**, **Cosmos DB**, **Azure Blob Storage**, **Event Hubs**, and **Service Bus**.

## Features

- **Fingerprint Generation** - Audio/video fingerprinting with blob storage
- **Fingerprint Matching** - Similarity search using Cosmos DB index
- **Claim Management** - Create and resolve content ID claims
- **Dispute Workflow** - Automated dispute resolution workflow via Service Bus
- **Batch + Stream Processing** - AKS jobs for fingerprinting, Event Hubs for match events
- **Idempotent Operations** - Redis-based deduplication via `Idempotency-Key` header
- **Outbox Pattern** - Reliable event publishing with transactional outbox
- **Resilience4j** - Circuit breaker, retry, timeout, bulkhead
- **OpenTelemetry** - Observability with Azure Monitor

## Architecture

### Hexagonal Architecture

```
┌─────────────────────────────────────────────────────┐
│                   Interfaces                         │
│         REST Controllers (OpenAPI/Swagger)          │
└─────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────┐
│               Application Layer                      │
│  Commands | DTOs | Mappers | Services (Use Cases)   │
└─────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────┐
│                  Domain Layer                        │
│ Entities | Value Objects | Repositories | Services  │
└─────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────┐
│              Infrastructure Layer                    │
│ JPA/Cosmos | Blob | Event Hubs | Service Bus        │
└─────────────────────────────────────────────────────┘
```

### Data Model

**Fingerprints** (PostgreSQL):
- Core fingerprint metadata
- Status tracking (PENDING, PROCESSED, FAILED)
- References to blob storage URIs

**Fingerprint Index** (Cosmos DB):
- Hash vectors for similarity search
- Partitioned for performance

**Matches** (PostgreSQL):
- Source and matched fingerprint relationships
- Match scores and timestamps
- Unique constraint on fingerprint pairs (idempotency)

**Claims** (PostgreSQL):
- Content ID claims with dispute status
- Associated matches
- Resolution tracking

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose (for local emulators)
- PostgreSQL 14+ (or Testcontainers for tests)
- Redis 6+ (or Testcontainers)
- Azurite (Azure Storage Emulator)
- Azure CLI (for cloud deployment)

### Local Development with Emulators

1. **Start emulators**:
```bash
docker-compose up -d
```

2. **Build the service**:
```bash
mvn clean package -DskipTests
```

3. **Run locally**:
```bash
java -jar target/content-id-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=local
```

The service will start on `http://localhost:8080`.

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DATABASE_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/contentid` |
| `DATABASE_USERNAME` | Database username | `postgres` |
| `DATABASE_PASSWORD` | Database password | `postgres` |
| `REDIS_HOST` | Redis host | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `AZURE_STORAGE_CONNECTION_STRING` | Blob storage connection | - |
| `AZURE_COSMOS_ENDPOINT` | Cosmos DB endpoint | - |
| `AZURE_COSMOS_KEY` | Cosmos DB key | - |
| `AZURE_EVENT_HUBS_CONNECTION_STRING` | Event Hubs connection | - |
| `AZURE_SERVICE_BUS_CONNECTION_STRING` | Service Bus connection | - |
| `AZURE_APP_CONFIG_CONNECTION_STRING` | App Configuration | - |
| `AZURE_KEY_VAULT_ENDPOINT` | Key Vault endpoint | - |
| `OAUTH2_ISSUER_URI` | OAuth2 issuer | - |
| `OAUTH2_JWK_SET_URI` | JWK set URI | - |

### Docker

```bash
# Build image
docker build -t content-id-service:latest .

# Run container
docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/contentid \
  -e REDIS_HOST=host.docker.internal \
  content-id-service:latest
```

### Kubernetes

See [k8s/](k8s/) directory for manifests. Deploy with:

```bash
kubectl apply -f k8s/
```

### Helm Chart

See [charts/content-id-service/](charts/content-id-service/) for Helm chart. Install with:

```bash
helm install content-id-service charts/content-id-service/
```

## API Endpoints

### Fingerprint API

- `POST /api/v1/fingerprint/{videoId}` - Create fingerprint for a video
- `GET /api/v1/fingerprint/{fingerprintId}` - Get fingerprint by ID

### Match API

- `POST /api/v1/match` - Find matches for a fingerprint
- `POST /api/v1/match/create` - Create a match record

### Claim API

- `POST /api/v1/claims` - Create a content ID claim
- `GET /api/v1/claims/{claimId}` - Get claim by ID
- `GET /api/v1/claims/video/{videoId}` - Get claims for a video
- `POST /api/v1/claims/{claimId}/resolve` - Resolve a claim

### API Documentation

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Patterns & Features

### Idempotency

All state-changing operations support idempotency via `Idempotency-Key` header:

```bash
curl -X POST http://localhost:8080/api/v1/fingerprint/{videoId} \
  -H "Idempotency-Key: abc123" \
  -H "Authorization: Bearer <token>"
```

Duplicate requests return `409 Conflict`.

### Outbox Pattern

Domain events are published via transactional outbox:
1. Event saved to `outbox_events` table in same transaction
2. Outbox dispatcher publishes to Service Bus/Event Hubs
3. Events marked as `DISPATCHED` after successful publish

### Resilience

- **Circuit Breaker**: Blob storage and match engine calls
- **Retry**: Transient failures (timeouts, network errors)
- **Timeout**: 10s for blob operations
- **Bulkhead**: Isolated thread pools for critical operations

### Observability

- **OpenTelemetry**: Automatic instrumentation
- **Azure Monitor**: Traces, metrics, logs
- **Correlation IDs**: Request tracing across services

## Testing

### Unit Tests

```bash
mvn test
```

### Integration Tests

```bash
mvn verify -P integration-test
```

Uses Testcontainers for:
- PostgreSQL
- Redis
- Azurite (Azure Storage Emulator)

### Contract Tests

```bash
mvn verify -P contract-test
```

## API Management (APIM) Policies

### JWT Validation

```xml
<policies>
  <inbound>
    <validate-jwt header-name="Authorization" failed-validation-httpcode="401">
      <openid-config url="https://your-tenant.b2clogin.com/your-tenant.onmicrosoft.com/v2.0/.well-known/openid-configuration" />
      <audiences>
        <audience>api://your-app-id</audience>
      </audiences>
      <issuers>
        <issuer>https://your-tenant.b2clogin.com/your-tenant-id/v2.0/</issuer>
      </issuers>
    </validate-jwt>
  </inbound>
</policies>
```

### Rate Limiting

```xml
<policies>
  <inbound>
    <rate-limit-by-key calls="100" renewal-period="60" counter-key="@(context.Request.IpAddress)" />
    <quota-by-key calls="10000" renewal-period="3600" counter-key="@(context.Request.IpAddress)" />
  </inbound>
</policies>
```

## Production Deployment

### Azure Kubernetes Service (AKS)

1. **Deploy to AKS**:
```bash
helm install content-id-service charts/content-id-service/ \
  --set image.repository=your-registry/content-id-service \
  --set azure.cosmos.endpoint=$AZURE_COSMOS_ENDPOINT \
  --set azure.serviceBus.connectionString=$AZURE_SERVICE_BUS_CONNECTION_STRING
```

2. **Scale**:
```bash
kubectl scale deployment content-id-service --replicas=5
```

### Health Checks

- Liveness: `/actuator/health/liveness`
- Readiness: `/actuator/health/readiness`
- Startup: `/actuator/health/startup`

## Monitoring

- **Application Insights**: Traces, metrics, logs
- **Azure Monitor**: Performance metrics, alerts
- **Grafana Dashboards**: Custom visualization

## License

Apache 2.0

