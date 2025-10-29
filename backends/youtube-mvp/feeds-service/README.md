# Feeds Service

A production-grade Spring Boot microservice implementing hexagonal architecture for personalized video feeds with ad slots.

## Architecture

- **Hexagonal Architecture**: Domain, Application, Infrastructure layers
- **DDD-lite**: Entities, value objects, repositories
- **CQRS**: Commands and queries separation
- **Resilience**: Retry, circuit breakers, timeouts via Resilience4j
- **Messaging**: Azure Service Bus for async event processing (fan-out pattern)

## Features

- GET `/api/v1/feeds/home` - Personalized home feed
- GET `/api/v1/feeds/subscriptions` - Subscriptions feed  
- GET `/api/v1/feeds/trending` - Trending feed

### Patterns

- **Precompute + Cache-aside**: Feeds cached in Redis for 30 minutes
- **Idempotent Fan-out**: Video published events trigger feed updates for subscribers
- **Backfill on cache miss**: Generate feed from video catalog when cache is empty
- **Ad injection**: Automatic ad slot insertion every 10 items

## Technology Stack

- **Java 17**, **Spring Boot 3.3.x**
- **Maven** for dependency management
- **Spring Cloud Azure** for Azure integration
- **Cosmos DB** for feed storage
- **Redis** for feed caching
- **Service Bus** for async messaging
- **Resilience4j** for resilience patterns
- **Springdoc OpenAPI** for API documentation
- **Problem Details (RFC7807)** for error handling

## Prerequisites

- Java 17+
- Maven 3.8+
- Docker (for local testing)
- Azure subscription (for deployment)

## Local Development

### With Emulators (Azurite/Cosmos Emulator)

1. **Start infrastructure**:
```bash
# Start Azurite (Storage emulator)
docker run -d -p 10000:10000 -p 10001:10001 -p 10002:10002 \
  --name azurite mcr.microsoft.com/azure-storage/azurite

# Start Cosmos DB emulator
docker run -d -p 8081:8081 \
  --name cosmos-emulator \
  mcr.microsoft.com/cosmosdb/linux/azure-cosmos-emulator

# Start Redis
docker run -d -p 6379:6379 --name redis redis:7-alpine
```

2. **Run the application**:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

3. **Access**:
- API: http://localhost:8080/api/v1/feeds/home
- Swagger UI: http://localhost:8080/swagger-ui.html
- Actuator: http://localhost:8080/actuator/health

### With Docker Compose

```bash
docker-compose up -d
```

## Building

### Maven Build

```bash
mvn clean package -DskipTests
```

### Docker Build

```bash
docker build -t feeds-service:latest .
```

## Testing

```bash
# Unit tests
mvn test

# Integration tests (requires infrastructure)
mvn verify -P integration-tests

# Test with coverage
mvn test jacoco:report
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Spring profile | `azure` |
| `REDIS_HOST` | Redis host | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `COSMOS_URI` | Cosmos DB endpoint | - |
| `COSMOS_KEY` | Cosmos DB key | - |
| `SERVICE_BUS_CONNECTION_STRING` | Service Bus connection string | - |

### Application Properties

See `application.yml` and `application-local.yml` for detailed configuration.

## Deployment

### Kubernetes

1. **Create namespace**:
```bash
kubectl create namespace youtube-mvp
```

2. **Create secrets**:
```bash
kubectl create secret generic feeds-service-secrets \
  --from-literal=redis-password='your-redis-password' \
  --from-literal=cosmos-key='your-cosmos-key' \
  --from-literal=servicebus-connection-string='your-servicebus-string' \
  --from-literal=app-insights-connection-string='your-app-insights-string' \
  -n youtube-mvp
```

3. **Apply manifests**:
```bash
kubectl apply -f k8s/
```

### Helm

```bash
helm install feeds-service ./charts/feeds-service \
  --namespace youtube-mvp \
  --set config.cosmos.uri=<your-cosmos-uri> \
  --set secrets.redis.password=<redis-password>
```

## Observability

### Metrics

- Prometheus metrics: `/actuator/prometheus`
- Azure Monitor integration enabled

### Logging

- Structured JSON logging
- Correlation IDs via OpenTelemetry

### Distributed Tracing

- OpenTelemetry instrumentation
- Traces exported to Azure Monitor

## API Documentation

Swagger UI: http://localhost:8080/swagger-ui.html

### Example Request

```bash
curl -X GET "http://localhost:8080/api/v1/feeds/home?pageSize=50" \
  -H "Authorization: Bearer <token>" \
  -H "If-None-Match: <etag>"
```

### Response

```json
{
  "items": [
    {
      "video_id": "video-123",
      "title": "Sample Video",
      "channel_id": "channel-456",
      "channel_name": "Sample Channel",
      "thumbnail_url": "https://...",
      "published_at": "2024-01-01T00:00:00Z",
      "view_count": 1000,
      "duration_seconds": 120,
      "is_ad": false
    }
  ],
  "last_updated": "2024-01-01T00:00:00Z",
  "etag": "etag-123",
  "total_count": 50,
  "page_size": 50,
  "next_page_token": null
}
```

## APIM Policies

### JWT Validation Policy

```xml
<policies>
  <inbound>
    <validate-jwt header-name="Authorization" failed-validation-httpcode="401">
      <openid-config url="https://login.microsoftonline.com/{tenantId}/v2.0/.well-known/openid-configuration" />
      <required-claims>
        <claim name="aud" match="any">
          <value>{client-id}</value>
        </claim>
      </required-claims>
    </validate-jwt>
  </inbound>
  <backend>
    <forward-request />
  </backend>
  <outbound />
  <on-error />
</policies>
```

### Rate Limiting Policy

```xml
<rate-limit calls="100" renewal-period="60" />
```

## Architecture Diagrams

See `docs/` directory for:
- Low-level design (LLD)
- Sequence diagrams for key flows

## Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature/my-feature`
3. Commit changes: `git commit -am 'Add feature'`
4. Push to branch: `git push origin feature/my-feature`
5. Submit pull request

## License

Copyright © 2024 YouTube MVP Team

