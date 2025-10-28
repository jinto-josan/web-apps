# Video Catalog Service

A production-grade microservice for managing video metadata in a YouTube-scale platform, built with Spring Boot 3.3.x, implementing Hexagonal Architecture, DDD-lite patterns, and CQRS.

## Features

- **Hexagonal Architecture**: Clean separation between domain, application, and infrastructure layers
- **DDD-lite**: Aggregates, value objects, repositories, and domain services
- **CQRS**: Separate command and query models for optimal read/write performance
- **Video State Management**: Draft → Publishing → Published workflow
- **Localization**: Multi-language support for titles and descriptions
- **API Versioning**: `/api/v1/videos`
- **ETag Support**: Conditional updates using `If-Match` header
- **Pagination**: Efficient pagination support
- **Reliable Messaging**: Outbox pattern with Azure Service Bus
- **Observability**: OpenTelemetry instrumentation for traces, metrics, and logs
- **Resilience**: Retry, circuit breaker, bulkhead, and rate limiting with Resilience4j
- **Security**: OIDC with Entra External ID (Azure AD B2C)

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                   Presentation Layer                         │
│  REST Controllers (ETag, Pagination, Validation)            │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                   Application Layer                          │
│  Command Service (CQRS Write) | Query Service (CQRS Read)  │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                      Domain Layer                            │
│  Video Aggregate | Value Objects | Domain Events            │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                 Infrastructure Layer                         │
│  Cosmos DB | Service Bus | Outbox | Search Index            │
└─────────────────────────────────────────────────────────────┘
```

## Tech Stack

- **Java 17**
- **Spring Boot 3.3.4**
- **Spring Cloud Azure 5.15.0**
- **Azure Cosmos DB**: Primary data store
- **Azure Service Bus**: Event streaming
- **Azure Cognitive Search**: Search index (optional)
- **Lombok**: Boilerplate reduction
- **MapStruct**: DTO mapping
- **Resilience4j**: Circuit breakers, retry, bulkhead, rate limiting
- **Springdoc OpenAPI**: API documentation
- **Problem Details (RFC 7807)**: Error handling
- **OpenTelemetry**: Distributed tracing
- **Testcontainers**: Integration testing

## Prerequisites

- Java 17
- Maven 3.9+
- Docker (for local Cosmos emulator)
- Azure subscription (for deployment)

## Local Development

### Using Cosmos Emulator

1. Start the Cosmos emulator:
```bash
docker run -p 8081:8081 -p 10251:10251 -p 10252:10252 \
  -m 2g mcr.microsoft.com/cosmosdb/linux/azure-cosmos-emulator:latest
```

2. Run the service:
```bash
make run
# or
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### Using Azure (Production-like)

Set environment variables:
```bash
export AZURE_COSMOS_ENDPOINT="https://your-account.documents.azure.com:443/"
export AZURE_COSMOS_KEY="your-key"
export AZURE_SERVICEBUS_CONNECTION_STRING="Endpoint=sb://..."
export OIDC_ISSUER_URI="https://your-tenant.b2clogin.com/..."
export OIDC_JWK_SET_URI="https://your-tenant.b2clogin.com/.../discovery/v2.0/keys"
```

Run:
```bash
mvn spring-boot:run
```

## API Documentation

Once running, access:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI Docs: http://localhost:8080/api-docs

## Key Endpoints

### Create Video
```bash
POST /api/v1/videos
Content-Type: application/json
Authorization: Bearer <token>

{
  "title": "My Video",
  "description": "Description",
  "channelId": "channel-123",
  "ownerId": "owner-123",
  "language": "en",
  "visibility": "PUBLIC",
  "tags": ["tech", "tutorial"],
  "localizedTitles": [
    {"language": "es", "text": "Mi Video"}
  ]
}
```

### Get Video
```bash
GET /api/v1/videos/{id}
If-None-Match: "v123"  # Optional: for conditional requests

# Response includes ETag header
ETag: "v123"
```

### Update Video
```bash
PATCH /api/v1/videos/{id}
If-Match: "v123"  # Required: prevents lost updates
Content-Type: application/json

{
  "title": "Updated Title",
  "description": "Updated Description"
}
```

### Publish Video
```bash
POST /api/v1/videos/{id}/publish

# Triggers:
# 1. State transition: DRAFT → PUBLISHED
# 2. Domain event: VideoPublishedEvent
# 3. Event published to Service Bus via Outbox pattern
# 4. Search index gets notified
```

### List Videos
```bash
GET /api/v1/videos?channelId=channel-123&page=0&size=20
GET /api/v1/videos?state=PUBLISHED&page=0&size=20
GET /api/v1/videos?visibility=PUBLIC&page=0&size=20
```

## Domain Model

### Video Aggregate
- **ID**: Unique identifier (`video-{uuid}`)
- **State**: DRAFT → PUBLISHING → PUBLISHED
- **Visibility**: PUBLIC, UNLISTED, PRIVATE
- **Metadata**: Title, description, tags, category
- **Localization**: Multi-language support
- **Metrics**: Views, likes, comments
- **Version**: For ETag/optimistic locking

### Value Objects
- `LocalizedText`: Language + text
- `Duration`: Time in seconds
- `VideoState`: Enum for lifecycle
- `VideoVisibility`: Enum for access control

## Domain Events

- **VideoPublishedEvent**: Raised when video transitions to published state
  - Published to Service Bus topic `video-published`
  - Consumed by search indexer, notifications, recommendations

## CQRS Implementation

### Command Side (Write)
- `VideoCommandService`: Create, update, publish, delete
- Validation, business rules, event generation
- Writes to Cosmos DB
- Publishes events via Outbox

### Query Side (Read)
- `VideoQueryService`: Get by ID, channel, state, visibility
- Optimized read models
- Pagination support

## Resilience Patterns

### Retry
```yaml
resilience4j:
  retry:
    instances:
      video-service:
        maxAttempts: 3
        waitDuration: 1s
```

### Circuit Breaker
```yaml
circuitbreaker:
  instances:
    video-service:
      slidingWindowSize: 100
      failureRateThreshold: 50
      waitDurationInOpenState: 10s
```

### Bulkhead
```yaml
bulkhead:
  instances:
    video-service:
      maxConcurrentCalls: 10
```

### Rate Limiter
```yaml
ratelimiter:
  instances:
    video-service:
      limitForPeriod: 100
      limitRefreshPeriod: 1s
```

## Outbox Pattern

Reliable event publishing using transactional outbox:

1. **Write to Outbox**: Save event to Cosmos DB in same transaction
2. **Background Processor**: Scheduled job picks up PENDING events
3. **Publish to Service Bus**: Send event and mark as PROCESSED
4. **Retry Logic**: Failed events retry up to 3 times
5. **Idempotent Consumers**: Events include deduplication keys

## Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn verify
```

Uses Testcontainers for Cosmos emulator integration.

### Contract Tests (Optional)
For CDC testing with other services using Pact.

## Docker

### Build
```bash
make docker-build
# or
docker build -t video-catalog-service:latest .
```

### Run
```bash
docker run -p 8080:8080 \
  -e AZURE_COSMOS_ENDPOINT="..." \
  -e AZURE_COSMOS_KEY="..." \
  video-catalog-service:latest
```

## Kubernetes Deployment

### Prerequisites
- Azure Kubernetes Service (AKS) cluster
- Azure Cosmos DB account
- Azure Service Bus namespace
- Managed Identity configured

### Deploy
```bash
make k8s-deploy
# or
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/secrets.yaml  # Create secrets first
```

### Secrets
Create secrets for Cosmos DB and Service Bus:
```bash
kubectl create secret generic cosmos-secrets \
  --from-literal=endpoint='https://...' \
  --from-literal=key='...' \
  -n youtube-mvp

kubectl create secret generic servicebus-secrets \
  --from-literal=connection-string='...' \
  -n youtube-mvp
```

### Scaling
```bash
kubectl scale deployment video-catalog-service -n youtube-mvp --replicas=5
```

HPA automatically scales based on CPU/memory (3-10 replicas).

## Observability

### Metrics
- Prometheus endpoint: http://localhost:8080/actuator/prometheus
- Custom metrics: video.create, video.publish, video.update

### Traces
- OpenTelemetry instrumentation
- Correlated spans across services
- Azure Monitor integration

### Logs
- Structured JSON logging
- Correlation IDs in MDC
- Log aggregation to Azure Log Analytics

## Configuration

### App Configuration
Uses Azure App Configuration for dynamic configuration:
```yaml
spring:
  cloud:
    azure:
      appconfiguration:
        enabled: true
        endpoint: https://your-config.azconfig.io
```

### Key Vault
Secrets fetched from Azure Key Vault:
```yaml
spring:
  cloud:
    azure:
      keyvault:
        secret:
          enabled: true
          endpoint: https://your-vault.vault.azure.net/
```

## APIM Integration

### API Management Policies

#### JWT Validation
```xml
<validate-jwt header-name="Authorization" failed-validation-httpcode="401">
    <openid-config url="https://your-tenant.b2clogin.com/your-tenant/b2c_1_signin/v2.0/.well-known/openid-configuration" />
    <required-claims>
        <claim name="aud" match="any">
            <value>your-client-id</value>
        </claim>
    </required-claims>
</validate-jwt>
```

#### Rate Limiting
```xml
<rate-limit-by-key calls="100" renewal-period="60" counter-key="@(context.Request.IpAddress)" />
```

#### CORS
```xml
<cors allow-credentials="true">
    <allowed-origins>
        <origin>https://example.com</origin>
    </allowed-origins>
    <allowed-methods>
        <method>GET</method>
        <method>POST</method>
        <method>PATCH</method>
    </allowed-methods>
</cors>
```

## Troubleshooting

### Health Checks
```bash
curl http://localhost:8080/actuator/health
```

### Logs
```bash
make logs
# or
kubectl logs -f -l app=video-catalog-service -n youtube-mvp
```

### Shell Access
```bash
make shell
# or
kubectl exec -it -l app=video-catalog-service -n youtube-mvp -- /bin/sh
```

## Performance Tuning

### Cosmos DB
- Partition key: `channelId` for optimal distribution
- Index policy for tags, title, category
- Consistency: SESSION for strong consistency where needed

### Service Bus
- Batching enabled for high throughput
- Deduplication enabled (7-day window)

### JVM Tuning
```bash
java -Xms2g -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -jar app.jar
```

## Security

- OIDC authentication with Entra External ID (B2C)
- JWT validation on all endpoints except health
- Role-based access control (RBAC)
- Network policies in Kubernetes
- Secrets in Key Vault

## Contributing

1. Create feature branch
2. Write tests
3. Ensure builds pass: `make test integration-test`
4. Submit PR

## License

MIT

