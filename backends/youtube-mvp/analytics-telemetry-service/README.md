# Analytics & Telemetry Service

Production-grade microservice for collecting client/server telemetry events and forwarding them to Azure Event Hubs (Kafka-compatible) with backpressure-safe batching, schema validation, and dead letter queue support.

## Architecture

- **Hexagonal Architecture**: Domain, Application, Infrastructure layers with clear separation of concerns
- **DDD-lite**: Entities, value objects, repositories
- **CQRS-ready**: Separation of command and query paths
- **Resilience**: Retry, circuit breakers, rate limiting via Resilience4j
- **Event-Driven**: Asynchronous event publishing to Azure Event Hubs
- **Observability**: OpenTelemetry instrumentation with Azure Monitor integration

## Features

- **POST `/api/v1/events/batch`** - Batch event collection (up to 1000 events per batch)
- **GET `/api/v1/events/stats`** - Service statistics and metrics
- **GET `/api/v1/events/health`** - Health check endpoint

### Key Patterns

- **Batching**: Events are batched (100 per batch by default) for efficient Event Hubs publishing
- **Idempotency**: Supports `Idempotency-Key` header and Redis-based deduplication
- **Schema Validation**: JSON Schema validation using Everit JSON Schema
- **Dead Letter Queue**: Failed events are stored in Azure Blob Storage for later analysis
- **Rate Limiting**: 10,000 events/second rate limit (configurable)
- **Backpressure-Safe**: Circuit breaker and retry mechanisms prevent cascading failures

## Technology Stack

- **Java 17**, **Spring Boot 3.3.x**
- **Maven** for dependency management
- **Spring Cloud Azure** for Azure integration
- **Azure Event Hubs** for event streaming (Kafka-compatible)
- **Azure Blob Storage** for dead letter queue
- **Redis** for idempotency tracking
- **Resilience4j** for resilience patterns
- **Springdoc OpenAPI** for API documentation
- **Problem Details (RFC7807)** for error handling
- **OpenTelemetry** for observability

## Prerequisites

- Java 17+
- Maven 3.8+
- Docker (for local testing)
- Azure subscription (for deployment)
- Event Hubs namespace and hub
- Azure Storage account (for DLQ)
- Redis instance

## Local Development

### With Docker Compose (Recommended)

```bash
# Start infrastructure (Redis, Azurite)
docker-compose up -d

# Build and run
make docker-build
make docker-run
```

Or use Docker Compose:

```bash
make docker-compose-up
```

### With Maven

```bash
# Start Redis
docker run -d -p 6379:6379 --name redis redis:7-alpine

# Start Azurite (Storage emulator)
docker run -d -p 10000:10000 -p 10001:10001 -p 10002:10002 \
  --name azurite mcr.microsoft.com/azure-storage/azurite

# Run the application
make run-local
```

### Configuration

Set environment variables for local development:

```bash
export REDIS_HOST=localhost
export REDIS_PORT=6379
export STORAGE_CONNECTION_STRING="DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=..."
export EVENT_HUBS_CONNECTION_STRING="Endpoint=sb://..."
export EVENT_HUB_NAME=telemetry-events
```

## API Usage

### Submit Events

```bash
curl -X POST http://localhost:8080/api/v1/events/batch \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Idempotency-Key: unique-request-id" \
  -d '{
    "events": [
      {
        "event_type": "video.view",
        "event_source": "web",
        "timestamp": "2024-01-15T10:30:00Z",
        "user_id": "user-123",
        "session_id": "session-456",
        "properties": {
          "video_id": "video-789",
          "duration": 120
        }
      }
    ]
  }'
```

### Get Statistics

```bash
curl -X GET http://localhost:8080/api/v1/events/stats \
  -H "Authorization: Bearer $TOKEN"
```

## Building

### Maven Build

```bash
make build
```

### Docker Build

```bash
make docker-build
```

## Testing

```bash
# Unit tests
make test

# Integration tests (requires Testcontainers)
make test-integration

# Coverage report
make coverage
```

## Deployment

### Kubernetes

```bash
# Install Helm chart
make install-k8s

# View logs
make logs-k8s

# Uninstall
make uninstall-k8s
```

### Manual K8s Deployment

```bash
kubectl apply -f k8s/
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `EVENT_HUBS_CONNECTION_STRING` | Event Hubs connection string | Required |
| `EVENT_HUB_NAME` | Event Hub name | `telemetry-events` |
| `STORAGE_CONNECTION_STRING` | Storage connection string | Required |
| `DLQ_CONTAINER_NAME` | DLQ container name | `telemetry-dlq` |
| `REDIS_HOST` | Redis host | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `REDIS_PASSWORD` | Redis password | - |
| `JWT_ISSUER_URI` | JWT issuer URI | Required |
| `APPLICATIONINSIGHTS_CONNECTION_STRING` | App Insights connection string | - |

### Application Properties

See `application.yml` for full configuration options including:
- Resilience4j settings (retry, circuit breaker, rate limiter)
- Event Hubs batch size
- Schema validation settings
- Idempotency TTL

## Observability

- **Metrics**: Exposed at `/actuator/prometheus`
- **Health**: `/actuator/health`
- **Traces**: OpenTelemetry traces sent to Azure Monitor
- **Logs**: Structured logging with correlation IDs

## API Management (APIM) Policy Snippets

### JWT Validation Policy

```xml
<validate-jwt header-name="Authorization" failed-validation-httpcode="401">
    <openid-config url="https://your-tenant.b2clogin.com/your-tenant.onmicrosoft.com/v2.0/.well-known/openid-configuration" />
    <required-claims>
        <claim name="scp" match="any">
            <value>telemetry.write</value>
        </claim>
    </required-claims>
</validate-jwt>
```

### Rate Limiting Policy

```xml
<rate-limit-by-key calls="10000" renewal-period="60" counter-key="@(context.Request.Headers.GetValueOrDefault("X-Forwarded-For", ""))" />
```

## Architecture Diagrams

See `docs/` directory for:
- `lld.md` - Low-level design with class diagrams
- `sequences/` - Sequence diagrams for key flows

## Troubleshooting

### Events Not Publishing

1. Check Event Hubs connection string
2. Verify circuit breaker status: `GET /actuator/health`
3. Check DLQ for failed events in Blob Storage

### Rate Limiting

Adjust `resilience4j.ratelimiter.instances.eventCollection.limit-for-period` in `application.yml`

### High Memory Usage

- Reduce batch size: `azure.eventhubs.batch-size`
- Check for memory leaks in event processing

## License

Apache 2.0

