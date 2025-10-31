# Observability & SRE Tools Service

Production-grade microservice for SLO management, synthetic checks, and observability configuration using **Java 17**, **Spring Boot 3.3.x**, **Azure Monitor**, **PostgreSQL**, and **Redis**.

## Features

- **SLO Management** - Create, track, and export Service Level Objectives
- **SLI Calculation** - Query Azure Monitor/Log Analytics to calculate SLIs
- **Error Budget Tracking** - Monitor error budget burn rate and remaining budget
- **Synthetic Checks** - HTTP/HTTPS synthetic monitoring with scheduled execution
- **Metrics Export** - Export SLO metrics to Prometheus and Azure Monitor
- **Scheduled Recalculation** - Automatic SLO recalculation based on SLI queries
- **Deep Health Checks** - Comprehensive health endpoint for dependency checks

## Architecture

### Hexagonal Architecture

```
┌─────────────────────────────────────────────────────┐
│                   Interfaces                         │
│         REST Controllers (SLO, Synthetic)            │
└─────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────┐
│               Application Layer                      │
│      Services, DTOs, Mappers (MapStruct)            │
└─────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────┐
│                  Domain Layer                        │
│    Entities, Value Objects, Repository Ports        │
└─────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────┐
│              Infrastructure Layer                    │
│  PostgreSQL, Azure Monitor, Prometheus, Schedulers   │
└─────────────────────────────────────────────────────┘
```

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose (for local emulators)
- Azure CLI (for cloud deployment)

### Local Development with Emulators

```bash
# Start emulators (PostgreSQL, Redis)
docker-compose up -d

# Build
mvn clean package -DskipTests

# Run
java -jar target/observability-service-0.1.0-SNAPSHOT.jar --spring.profiles.active=local
```

The service will start on `http://localhost:8080`.

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DATASOURCE_URL` | PostgreSQL connection URL | - |
| `DATASOURCE_USERNAME` | PostgreSQL username | - |
| `DATASOURCE_PASSWORD` | PostgreSQL password | - |
| `REDIS_HOST` | Redis host | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `AZURE_MONITOR_WORKSPACE_ID` | Log Analytics workspace ID | - |
| `APPLICATIONINSIGHTS_CONNECTION_STRING` | Application Insights connection string | - |
| `JWT_ISSUER_URI` | OAuth2 issuer URI | - |
| `OAUTH2_JWK_SET_URI` | JWK set URI | - |

### Docker

```bash
# Build image
docker build -t observability-service:latest .

# Run with env file
docker run --env-file .env -p 8080:8080 observability-service:latest
```

### Kubernetes

```bash
# Apply secrets and configmaps first
kubectl apply -f k8s/secrets.yaml
kubectl apply -f k8s/configmap.yaml

# Deploy
kubectl apply -f k8s/

# Check status
kubectl get pods -l app=observability-service

# View logs
kubectl logs -f deployment/observability-service
```

## API Documentation

### Endpoints

#### Create SLO
```http
POST /api/v1/slo
Authorization: Bearer {jwt}
Content-Type: application/json

{
  "name": "API Availability",
  "serviceName": "video-service",
  "description": "99.9% availability target",
  "slis": [
    {
      "name": "availability",
      "type": "AVAILABILITY",
      "query": "traces | where timestamp > ago(30d) | summarize availability = (countif(success == true) * 100.0 / count())"
    }
  ],
  "targetPercent": 99.9,
  "timeWindow": "30d",
  "labels": {
    "team": "platform",
    "tier": "critical"
  }
}
```

#### Get SLO
```http
GET /api/v1/slo/{sloId}
Authorization: Bearer {jwt}
```

#### Get All SLOs
```http
GET /api/v1/slo?serviceName=video-service
Authorization: Bearer {jwt}
```

#### Recalculate SLO
```http
POST /api/v1/slo/{sloId}/recalculate
Authorization: Bearer {jwt}
```

#### Create Synthetic Check
```http
POST /api/v1/synthetics
Authorization: Bearer {jwt}
Content-Type: application/json

{
  "name": "Homepage Check",
  "description": "Check homepage availability",
  "type": "HTTPS",
  "endpoint": "https://www.youtube-mvp.com",
  "method": "GET",
  "expectedStatusCode": 200,
  "timeoutSeconds": 10,
  "intervalSeconds": 60,
  "enabled": true
}
```

#### Run Synthetic Check
```http
POST /api/v1/synthetics/{checkId}/run
Authorization: Bearer {jwt}
```

#### Deep Health Check
```http
GET /api/v1/healthz/deep
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
- Azure Monitor emulator (mocked)

## Monitoring & Observability

### Metrics (Prometheus)
- `http://localhost:8080/actuator/metrics`
- Custom metrics:
  - `slo_target_percent` - SLO target percentage
  - `slo_current_percent` - Current SLO value
  - `slo_error_budget_total` - Total error budget
  - `slo_error_budget_remaining` - Remaining error budget

### Health
- Liveness: `http://localhost:8080/actuator/health/liveness`
- Readiness: `http://localhost:8080/actuator/health/readiness`
- Deep: `http://localhost:8080/api/v1/healthz/deep`

### Traces
OpenTelemetry traces exported to Azure Monitor.

## Production Deployment

### Azure Container Apps

```bash
az containerapp create \
  --name observability-service \
  --resource-group youtube-mvp \
  --image <registry>/observability-service:latest \
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

## SLO Calculation

SLOs are calculated by:
1. Querying Azure Monitor/Log Analytics for SLI data
2. Aggregating SLI values (currently simple average)
3. Comparing against target percentage
4. Calculating error budget burn rate
5. Exporting metrics to Prometheus/Azure Monitor

## Synthetic Checks

Synthetic checks run on a scheduled interval and:
- Execute HTTP/HTTPS requests
- Validate response status codes
- Optionally validate response body patterns
- Store results with metadata
- Can be manually triggered via API

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
- SLO calculation flow
- Synthetic check execution
- Error budget tracking
- Metrics export

## License

MIT

