# Experimentation Service

Production-grade microservice for feature flags and experiment management using **Java 17**, **Spring Boot 3.3+**, **Azure App Configuration**, **Cosmos DB**, and **Redis**.

## Features

- **Feature Flags** - Serve feature flags from Azure App Configuration with rollout percentages and conditions
- **Experiments** - A/B testing with deterministic bucketing, sticky assignments, and variant management
- **Cohort Tracking** - Store user-experiment assignments in Cosmos DB with Redis caching
- **Deterministic Bucketing** - Consistent user assignment across requests using MD5 hashing
- **Sticky Assignments** - Users stay in the same variant for the experiment duration
- **Cache Invalidation** - Automatic cache refresh on App Configuration updates
- **Resilience4j** - Circuit breaker, retry, and timeout for external dependencies
- **Observability** - OpenTelemetry integration with Azure Monitor

## Architecture

### Hexagonal Architecture

```
┌─────────────────────────────────────────────────────┐
│              Infrastructure Layer                    │
│   Controllers | Filters | Config | Exception Handlers│
└─────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────┐
│               Application Layer                      │
│      Services | DTOs | Mappers                       │
└─────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────┐
│                  Domain Layer                        │
│    Entities | Value Objects | Repository Interfaces │
│    Domain Services (Bucketing)                      │
└─────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────┐
│              Infrastructure Layer                    │
│  Cosmos DB | Redis | App Config | Adapters         │
└─────────────────────────────────────────────────────┘
```

### Data Model

**Experiments** (Cosmos DB):
- Partition Key: `key`
- Container: `experiments`
- Stores experiment configuration and variants

**Cohorts** (Cosmos DB):
- Partition Key: `userId`
- Container: `cohorts`
- Stores user-experiment assignments for sticky experiments

**Feature Flags** (Azure App Configuration):
- Managed via Azure Portal/CLI
- Automatically synced and cached in Redis

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose (for local emulators)
- Azure CLI (for cloud deployment)

### Local Development

```bash
# Start Redis
docker run -d -p 6379:6379 redis:7-alpine

# Build
mvn clean package -DskipTests

# Run
java -jar target/experimentation-service-0.1.0-SNAPSHOT.jar --spring.profiles.active=local
```

The service will start on `http://localhost:8080`.

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `AZURE_COSMOS_ENDPOINT` | Cosmos DB endpoint | - |
| `AZURE_COSMOS_KEY` | Cosmos DB key | - |
| `AZURE_COSMOS_DB` | Database name | `experimentation` |
| `AZURE_APP_CONFIG_ENDPOINT` | App Configuration endpoint | - |
| `AZURE_APP_CONFIG_CONNECTION_STRING` | App Config connection string | - |
| `REDIS_HOST` | Redis host | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `OAUTH2_ISSUER_URI` | OAuth2 issuer | - |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | OpenTelemetry endpoint | `http://localhost:4318` |

### Docker

```bash
# Build image
docker build -t experimentation-service:latest .

# Run
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=local \
  -e REDIS_HOST=host.docker.internal \
  experimentation-service:latest
```

### Kubernetes

```bash
# Deploy
kubectl apply -f k8s/

# Or use Helm
helm install experimentation-service charts/experimentation-service
```

## API Endpoints

### Get Feature Flags

```bash
GET /api/v1/flags?userId=user123&region=us

Response:
[
  {
    "key": "new-ui",
    "enabled": true,
    "metadata": {}
  }
]
```

### Get Specific Feature Flag

```bash
GET /api/v1/flags/{key}?userId=user123

Response:
{
  "key": "new-ui",
  "enabled": true,
  "metadata": {}
}
```

### Get Experiment Variant

```bash
GET /api/v1/experiments/{key}?userId=user123

Response:
{
  "key": "recommendation-algo",
  "variantId": "variant-2",
  "variantName": "Treatment",
  "configuration": {
    "algorithm": "neural",
    "features": ["watch_history", "likes"]
  },
  "metadata": {}
}
```

## Bucketing Algorithm

The service uses MD5 hashing for deterministic bucketing:

1. Combine `userId` and `experimentKey`: `userId:experimentKey`
2. Compute MD5 hash
3. Map to bucket 0-9999
4. Assign variant based on cumulative traffic percentages

This ensures:
- Same user always gets same bucket for same experiment
- Uniform distribution across users
- Consistent assignment across service restarts

## Cache Strategy

- **Feature Flags**: Cached in Redis with 1-hour TTL
- **Experiments**: Cached in Redis with 1-hour TTL
- **Cohorts**: Cached in Redis for sticky assignments
- **Invalidation**: Automatic on App Configuration refresh events

## Resilience

- **Retry**: 3 attempts with exponential backoff for App Configuration
- **Circuit Breaker**: Opens after 50% failure rate (10 request window)
- **Timeout**: 3s timeout for App Configuration calls
- **Fallback**: Returns disabled flag if App Configuration unavailable

## Observability

- **Traces**: OpenTelemetry auto-instrumentation
- **Metrics**: Spring Actuator + Prometheus
- **Logs**: Structured logging with correlation IDs
- **Correlation ID**: Passed via `X-Correlation-ID` header

## Health Checks

The service exposes health check endpoints:

- `GET /actuator/health` - Overall health
- `GET /actuator/health/liveness` - Liveness probe (Kubernetes)
- `GET /actuator/health/readiness` - Readiness probe (Kubernetes)

Health checks verify:
- Redis connectivity
- Cosmos DB connectivity
- App Configuration availability (if enabled)

## API Management (APIM) Policies

### JWT Validation

```xml
<validate-jwt header-name="Authorization" failed-validation-httpcode="401">
    <openid-config url="https://login.microsoftonline.com/{tenant}/v2.0/.well-known/openid-configuration" />
    <required-claims>
        <claim name="aud" match="any">
            <value>{client-id}</value>
        </claim>
    </required-claims>
</validate-jwt>
```

### Rate Limiting

```xml
<rate-limit-by-key calls="100" renewal-period="60" counter-key="@(context.Request.Headers.GetValueOrDefault("Authorization","").AsJwt()?.Subject)" />
```

## Testing

```bash
# Unit tests
mvn test

# Integration tests (requires Testcontainers)
mvn verify

# Coverage report
mvn jacoco:report
```

## Documentation

- [Low-Level Design](./docs/lld.md)
- [Sequence Diagrams](./docs/sequences/)

## License

MIT

