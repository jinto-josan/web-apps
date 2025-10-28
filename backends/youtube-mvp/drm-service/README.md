# DRM Service

Production-grade microservice for managing DRM (Digital Rights Management) policies with Azure Media Services integration, key rotation, and audit trail.

## Features

- **DRM Policy Management** - Create, update, and manage DRM policies for videos (Widevine, PlayReady, FairPlay)
- **Azure Media Services Integration** - Automatic content key policy management via AMS
- **Key Rotation** - Automated and manual key rotation with scheduled jobs
- **Audit Trail** - Complete audit logging for all policy changes and operations
- **Idempotent Operations** - Support for idempotency keys to prevent duplicate operations
- **Cache-Aside Pattern** - Redis caching for improved performance
- **Resilience4j** - Circuit breaker, retry, rate limiter for external calls
- **Observability** - OpenTelemetry integration with Azure Monitor

## Architecture

### Hexagonal Architecture

```
┌─────────────────────────────────────────────────────┐
│                   Interfaces                         │
│         REST Controllers | APIs                     │
└─────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────┐
│               Application Layer                      │
│      Commands | Queries | Use Cases                 │
└─────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────┐
│                  Domain Layer                        │
│    Entities | Value Objects | Repositories (Ports)   │
└─────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────┐
│              Infrastructure Layer                    │
│  PostgreSQL | Redis | Service Bus | AMS Adapters   │
└─────────────────────────────────────────────────────┘
```

### Data Model

**DRM Policies** (PostgreSQL):
- Policy configuration with provider-specific settings
- Key rotation policy configuration
- Optimistic locking via version field
- Full audit trail

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose (for local emulators)
- PostgreSQL
- Redis
- Azure CLI (for cloud deployment)

### Local Development

```bash
# Start local services (PostgreSQL, Redis)
docker-compose up -d

# Run migrations
mvn flyway:migrate

# Build
mvn clean package -DskipTests

# Run
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

The service will start on `http://localhost:8080`.

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `AZURE_COSMOS_ENDPOINT` | Cosmos DB endpoint | - |
| `AZURE_COSMOS_KEY` | Cosmos DB key | - |
| `AZURE_SERVICE_BUS_CONNECTION_STRING` | Service Bus connection | - |
| `AZURE_KEY_VAULT_ENDPOINT` | Key Vault endpoint | - |
| `AZURE_APP_CONFIG_ENDPOINT` | App Configuration endpoint | - |
| `AZURE_MEDIA_SERVICES_ENDPOINT` | Media Services endpoint | - |
| `DATASOURCE_URL` | PostgreSQL connection URL | - |
| `DATASOURCE_USERNAME` | PostgreSQL username | - |
| `DATASOURCE_PASSWORD` | PostgreSQL password | - |
| `REDIS_HOST` | Redis host | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `OAUTH2_ISSUER_URI` | OAuth2 issuer | - |
| `OAUTH2_JWK_SET_URI` | JWK set URI | - |

### Docker

```bash
# Build image
docker build -t drm-service:latest .

# Run with env file
docker run --env-file .env drm-service:latest
```

### Kubernetes

```bash
# Create namespace
kubectl create namespace youtube-mvp

# Create secrets
kubectl create secret generic drm-service-secrets --from-env-file=.env -n youtube-mvp

# Deploy
kubectl apply -f k8s/

# Check status
kubectl get pods -l app=drm-service -n youtube-mvp
kubectl logs -f deployment/drm-service -n youtube-mvp

# Access service
kubectl port-forward svc/drm-service 8080:80 -n youtube-mvp
```

## API Documentation

### Endpoints

#### Create DRM Policy
```http
POST /api/v1/drm/policies
Idempotency-Key: {uuid}
Authorization: Bearer {jwt}
Content-Type: application/json

{
  "videoId": "video-123",
  "provider": "WIDEVINE",
  "contentKeyPolicyName": "my-policy",
  "licenseConfiguration": {
    "enablePersistentLicense": "true"
  },
  "allowedApplications": ["com.example.app"],
  "persistentLicenseAllowed": true,
  "rotationPolicy": {
    "enabled": true,
    "rotationInterval": "P30D",
    "rotationKeyVaultUri": "https://vault.vault.azure.net/keys/drm-keys/"
  }
}
```

#### Update DRM Policy
```http
PUT /api/v1/drm/policies/{policyId}
If-Match: {version}
Idempotency-Key: {uuid}
Authorization: Bearer {jwt}
Content-Type: application/json

{
  "contentKeyPolicyName": "updated-policy",
  "licenseConfiguration": {
    "enablePersistentLicense": "false"
  }
}
```

#### Get DRM Policy
```http
GET /api/v1/drm/policies/{policyId}
Authorization: Bearer {jwt}
```

#### Get DRM Policy by Video ID
```http
GET /api/v1/drm/policies/video/{videoId}
Authorization: Bearer {jwt}
```

#### Rotate Keys
```http
POST /api/v1/drm/policies/rotate-keys
Authorization: Bearer {jwt}
Content-Type: application/json

{
  "policyIds": ["policy-1", "policy-2"]
}
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
- PostgreSQL
- Redis
- Azure Cosmos DB emulator
- Azure Service Bus emulator (Azurite)

## Monitoring & Observability

### Metrics (Prometheus)
- `http://localhost:8080/actuator/metrics`
- Custom: `drm.policy.operations`, `drm.policy.rotation.count`

### Health
- Liveness: `http://localhost:8080/actuator/health/liveness`
- Readiness: `http://localhost:8080/actuator/health/readiness`

### Traces
OpenTelemetry traces exported to Azure Monitor with correlation IDs.

## Key Rotation Scheduler

The service includes a scheduled job that automatically rotates keys for policies where:
- Key rotation is enabled
- Next rotation time has been reached

Default schedule: Every 30 minutes (configurable via `drm.key-rotation.schedule`)

## Production Deployment

### Azure Container Apps

```bash
az containerapp create \
  --name drm-service \
  --resource-group youtube-mvp \
  --image <registry>/drm-service:latest \
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

#### Idempotency Support
```xml
<set-header name="Idempotency-Key" exists-action="override">
    <value>@(Guid.NewGuid().ToString())</value>
</set-header>
```

## Makefile

```bash
# Development
make run-local          # Run with local emulators
make test              # Run tests
make build             # Build JAR
make clean             # Clean build artifacts

# Docker
make docker-build      # Build Docker image
make docker-run        # Run Docker container
make docker-push       # Push to registry

# K8s
make k8s-deploy        # Deploy to Kubernetes
make k8s-logs          # View logs
make k8s-undeploy      # Undeploy from Kubernetes

# Database
make db-migrate        # Run database migrations
make db-rollback       # Rollback last migration
```

## Sequence Diagrams

See `docs/sequences/` for detailed flow diagrams:
- DRM policy creation flow
- Key rotation flow
- Policy update with AMS integration
- Audit trail flow

## License

MIT

