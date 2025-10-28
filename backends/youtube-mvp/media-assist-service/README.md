# Media Assist Service

Production-grade microservice for managing Azure Blob Storage assets, SAS issuance, and secure media access.

## Overview

This service provides:
- Azure Blob Storage asset management
- SAS (Shared Access Signature) URL generation for secure access
- Signed playback URLs for media streaming
- Path normalization and validation
- Audit logging for compliance
- Policy-based access control
- Idempotent operations via Idempotency-Key header

## Architecture

### Hexagonal Architecture

```
┌─────────────────────────────────────────┐
│         Interfaces (REST)               │
├─────────────────────────────────────────┤
│         Application (Use Cases)         │
├─────────────────────────────────────────┤
│         Domain (Business Logic)         │
├─────────────────────────────────────────┤
│      Infrastructure (Adapters)          │
│  - Azure Blob Storage Adapter          │
│  - Redis (Idempotency, Cache)           │
│  - Service Bus (Audit Logs)             │
└─────────────────────────────────────────┘
```

## Technology Stack

- **Java 17**
- **Spring Boot 3.3.x**
- **Spring Cloud Azure 5.14.0**
- **Azure Blob Storage SDK**
- **Redis** (Lettuce)
- **Resilience4j** (Circuit Breaker, Retry, Rate Limiter)
- **OAuth2 Resource Server** (Entra External ID/B2C)
- **OpenTelemetry** (Azure Monitor)

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.9+
- Docker & Docker Compose
- Azure CLI (for cloud deployment)

### Local Development

1. **Start emulators:**
   ```bash
   docker-compose up -d
   ```

2. **Run the service:**
   ```bash
   make run
   ```

3. **Or build and run with Maven:**
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```

### Running Tests

```bash
make test

# Integration tests with Testcontainers
make integration-test
```

## API Endpoints

### Generate SAS URL

```bash
POST /api/v1/media/sas
Content-Type: application/json
Authorization: Bearer <token>
Idempotency-Key: <uuid>  # Optional

{
  "path": "videos/abc123.mp4",
  "container": "renditions",
  "validityDuration": "PT1H",
  "playback": false
}
```

**Response:**
```json
{
  "url": "https://...blob.core.windows.net/renditions/videos/abc123.mp4?sv=...",
  "expiresAt": "2024-01-01T12:00:00Z"
}
```

### Get Signed Origin URL

```bash
GET /api/v1/media/origin/{path}?container=renditions&validity=PT4H
Authorization: Bearer <token>
Idempotency-Key: <uuid>
```

## Configuration

### Environment Variables

```bash
# Azure Storage
AZURE_STORAGE_ACCOUNT_NAME=your-storage-account
AZURE_STORAGE_ACCOUNT_KEY=your-key
AZURE_STORAGE_BLOB_ENDPOINT=https://your-account.blob.core.windows.net

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# OAuth2
OAUTH2_ISSUER_URI=https://your-tenant.b2clogin.com/tenant/your-policy
OAUTH2_JWK_SET_URI=https://your-tenant.b2clogin.com/tenant/your-policy/discovery/v2.0/keys

# Azure App Configuration
AZURE_APP_CONFIG_ENDPOINT=https://your-config.azconfig.io

# Azure Key Vault
AZURE_KEY_VAULT_ENDPOINT=https://your-vault.vault.azure.net
```

### Application Properties

See `application.yml` and `application-local.yml` for full configuration.

## Docker Deployment

```bash
# Build
make docker-build

# Run
make docker-run

# Down
docker-compose down
```

## Kubernetes Deployment

```bash
# Apply K8s manifests
kubectl apply -f k8s/

# Using Helm
helm install media-assist-service charts/
```

## API Management (APIM) Integration

### JWT Validation Policy

```xml
<policies>
  <inbound>
    <validate-jwt header-name="Authorization" 
                  failed-validation-httpcode="401" 
                  failed-validation-error-message="Unauthorized">
      <openid-config url="https://your-tenant.b2clogin.com/tenant/your-policy/.well-known/openid-configuration" />
      <required-claims>
        <claim name="aud" match="all">
          <value>your-client-id</value>
        </claim>
      </required-claims>
    </validate-jwt>
    
    <rate-limit calls="100" renewal-period="60" />
    <quota calls="10000" renewal-period="3600" />
    
    <base />
  </inbound>
  <backend>
    <forward-request />
  </backend>
</policies>
```

### Rate Limiting

- **Per subscription:** 100 calls/minute
- **Per IP:** 50 calls/minute
- **Quota:** 10,000 calls/hour

## Security

- OAuth2 Resource Server (JWT validation)
- Path traversal protection
- HTTPS enforcement for SAS URLs
- Audit logging for all access
- Idempotency via Redis deduplication

## Observability

### OpenTelemetry

Traces, metrics, and logs are exported to Azure Monitor:

```bash
# View logs
az monitor log-analytics query \
  --workspace <workspace-id> \
  --analytics-query "AppTraces | where AppRoleName == 'media-assist-service'"
```

### Metrics

- `http.server.requests` - HTTP request metrics
- `resilience4j.circuitbreaker.calls` - Circuit breaker metrics
- `blob_storage_operations` - Blob operation counts

### Health Checks

```bash
curl http://localhost:8080/actuator/health
```

## Resilience

- **Circuit Breaker:** Fail-fast after 50% failure rate
- **Retry:** Exponential backoff, max 3 attempts
- **Timeout:** 5s for blob operations
- **Bulkhead:** Isolated thread pools

## License

MIT

