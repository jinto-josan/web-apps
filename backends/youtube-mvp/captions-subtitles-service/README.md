# Captions and Subtitles Service

Production-grade microservice for managing video captions and subtitles with auto STT, human editing, and translations.

## Features

- 🎤 **Auto Speech-to-Text** via Azure AI Speech
- ✏️ **Human Caption Editing** with versioning support
- 🌐 **Multi-language Translations** via Azure Translator
- 💾 **SRT/WebVTT Storage** in Azure Blob Storage
- 📊 **Metadata Management** in Cosmos DB
- 🔄 **Event Publishing** via Azure Service Bus
- 🔒 **Idempotent Processing** with Redis deduplication
- 🏷️ **ETag Support** for caching and version control
- 🛡️ **Resilience4j** for circuit breakers, retries, and timeouts
- 📡 **OpenTelemetry** observability

## Architecture

### Hexagonal Architecture

```
domain/              # Domain layer (entities, value objects, ports)
application/         # Application layer (use cases, services)
infrastructure/      # Infrastructure layer (adapters, persistence)
interfaces/          # Interface layer (REST controllers)
```

### Technology Stack

- **Java 17** with Spring Boot 3.3.x
- **Maven** for dependency management
- **Spring Cloud Azure** for Azure integrations
- **Resilience4j** for resilience patterns
- **OpenTelemetry** for distributed tracing
- **Testcontainers** for integration testing

## Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose (for local development)
- Azure subscriptions for:
  - Azure AI Speech
  - Azure Translator
  - Azure Blob Storage
  - Azure Cosmos DB
  - Azure Service Bus

## Local Development

### Quick Start

1. **Clone and build**:
   ```bash
   cd captions-subtitles-service
   mvn clean install
   ```

2. **Start emulators** (using Testcontainers):
   ```bash
   make run-local
   ```

3. **Access services**:
   - API: http://localhost:8080/api/v1/captions
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - Health: http://localhost:8080/api/v1/health

### Environment Variables

Set in `application-local.yml` or via environment:

```bash
# Azure Cosmos DB
export AZURE_COSMOS_ENDPOINT="..."
export AZURE_COSMOS_KEY="..."

# Azure Storage
export AZURE_STORAGE_CONNECTION_STRING="..."

# Azure AI Speech
export AZURE_SPEECH_KEY="..."
export AZURE_SPEECH_REGION="centralus"

# Azure Translator
export AZURE_TRANSLATOR_ENDPOINT="..."
export AZURE_TRANSLATOR_KEY="..."

# OAuth2
export OAUTH2_ISSUER_URI="..."
export OAUTH2_JWK_SET_URI="..."
```

### With Docker

```bash
# Build
docker build -t captions-subtitles-service .

# Run
docker run -p 8080:8080 \
  -e AZURE_COSMOS_ENDPOINT=$AZURE_COSMOS_ENDPOINT \
  -e AZURE_COSMOS_KEY=$AZURE_COSMOS_KEY \
  captions-subtitles-service
```

## API Endpoints

### Captions Management

```bash
# List captions for a video
GET /api/v1/videos/{videoId}/captions

# Get caption by ID (with ETag support)
GET /api/v1/captions/{captionId}
Headers: If-None-Match: "etag-value"

# Download caption content
GET /api/v1/captions/{captionId}/content

# Auto-generate captions
POST /api/v1/videos/{videoId}/captions/auto
  ?audioUri=<audio-uri>
  &language=en

# Upload manual captions
POST /api/v1/videos/{videoId}/captions
  ?language=en
  &format=WebVTT
  Body: Multipart file upload

# Translate caption
POST /api/v1/captions/{captionId}/translate?targetLanguage=es
```

## Resilience Configuration

### Circuit Breaker

Configured for STT and Translator services:

```yaml
resilience4j:
  circuitbreaker:
    instances:
      stt-service:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
```

### Retry

Exponential backoff with configurable attempts:

```yaml
resilience4j:
  retry:
    instances:
      stt-service:
        maxAttempts: 3
        waitDuration: 2s
        exponentialBackoffMultiplier: 2
```

### Timeout

Prevent hanging requests:

```yaml
resilience4j:
  timelimiter:
    instances:
      stt-service:
        timeoutDuration: 30s
```

## Kubernetes Deployment

### Helm Chart

```bash
helm install captions-service ./charts/captions-service \
  --namespace youtube \
  --set azure.cosmos.endpoint=$AZURE_COSMOS_ENDPOINT
```

### K8s Manifests

Located in `k8s/` directory:
- `deployment.yaml` - Main deployment
- `service.yaml` - Service definition
- `hpa.yaml` - Horizontal Pod Autoscaler
- `network-policy.yaml` - Network policies
- `poddisruptionbudget.yaml` - PDB for availability

## Testing

### Unit Tests

```bash
mvn test
```

### Integration Tests

```bash
mvn verify  # Includes Testcontainers tests
```

### Load Testing

Example with Apache Bench:

```bash
ab -n 1000 -c 10 \
  -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/videos/v123/captions
```

## API Management (APIM)

### JWT Validation Policy

```xml
<validate-jwt header-name="Authorization">
    <openid-config url="https://login.microsoftonline.com/{tenant}/v2.0/.well-known/openid-configuration" />
    <audiences>
        <audience>api://captions-service</audience>
    </audiences>
    <issuers>
        <issuer>https://login.microsoftonline.com/{tenant}/v2.0</issuer>
    </issuers>
</validate-jwt>
```

### Rate Limiting Policy

```xml
<rate-limit calls="100" renewal-period="60" />
<cache-lookup vary-by-developer="false" vary-by-developer-groups="false">
    <vary-by-header>Accept</vary-by-header>
</cache-lookup>
```

## Monitoring & Observability

### Metrics

- Prometheus metrics exposed at `/actuator/prometheus`
- Custom metrics for caption generation success rates

### Traces

- OpenTelemetry traces exported to Azure Monitor
- Correlation IDs propagated across services

### Logs

- Structured JSON logging
- Log levels configurable via environment

## Security

- **OAuth2 Resource Server** with JWT validation
- **Managed Identity** for Azure service authentication
- **Network Policies** for pod-to-pod communication
- **ETag** for optimistic concurrency control
- **Idempotency** via Redis deduplication

## Performance

- **Cache-aside** pattern with Redis
- **Connection pooling** for Azure services
- **Async processing** for batch operations
- **Content compression** for blob storage

## License

Copyright © 2024 YouTube MVP. All rights reserved.
