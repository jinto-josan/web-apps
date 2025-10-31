# Edge/CDN Control Service

Production-grade microservice for managing Azure Front Door/CDN rules, WAF configurations, and origin failover policies.

## Overview

This service provides a centralized API for:
- Managing CDN/Front Door routing rules
- Configuring WAF policies
- Setting up origin failover policies
- Cache purging operations
- Configuration drift detection
- Change validation and rollback support

## Architecture

- **Java 17** + **Spring Boot 3.3.x** + **Maven**
- **Hexagonal Architecture** (Domain, Application, Infrastructure layers)
- **DDD-lite** with entities, value objects, and repositories
- **CQRS** for read operations
- **Lombok** + **MapStruct** for DTOs/mappers
- **ProblemDetails (RFC7807)** error handling
- **Spring Security OAuth2** Resource Server (Entra External ID/B2C)
- **Spring Cloud Azure** integration
- **Resilience4j** (circuit breaker, retry, timeout, bulkhead, rate limiter)
- **OpenTelemetry** observability

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.9+
- Docker (for local services)
- Azure subscription with Front Door/CDN resources
- PostgreSQL 15+
- Redis 7+

### Local Development

1. **Start local services:**
   ```bash
   make local-services
   ```

2. **Set environment variables:**
   ```bash
   export AZURE_SUBSCRIPTION_ID=<your-subscription-id>
   export AZURE_TENANT_ID=<your-tenant-id>
   export AZURE_CLIENT_ID=<your-client-id>
   export AZURE_CLIENT_SECRET=<your-client-secret>
   export POSTGRES_USER=postgres
   export POSTGRES_PASSWORD=postgres
   export REDIS_HOST=localhost
   export REDIS_PORT=6379
   ```

3. **Run the service:**
   ```bash
   make run
   ```

   Or with Maven:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```

4. **Access Swagger UI:**
   ```
   http://localhost:8080/swagger-ui
   ```

### Docker

```bash
# Build
make docker-build

# Run
docker run -p 8080:8080 \
  -e AZURE_SUBSCRIPTION_ID=<id> \
  -e AZURE_TENANT_ID=<id> \
  -e AZURE_CLIENT_ID=<id> \
  -e AZURE_CLIENT_SECRET=<secret> \
  edge-cdn-control-service:latest
```

### Kubernetes

```bash
# Deploy using kubectl
make deploy

# Or using Helm
make helm-install
```

## API Endpoints

### CDN Rules

- `POST /api/v1/cdn/rules` - Create a new CDN rule
- `GET /api/v1/cdn/rules` - List CDN rules (with pagination)
- `GET /api/v1/cdn/rules/{ruleId}` - Get rule by ID
- `POST /api/v1/cdn/rules/{ruleId}/apply` - Apply a rule to Azure Front Door
- `POST /api/v1/cdn/rules/{ruleId}/detect-drift` - Detect configuration drift

### Cache Purge

- `POST /api/v1/cdn/purge` - Purge CDN cache

## Configuration

### Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `AZURE_SUBSCRIPTION_ID` | Azure subscription ID | Yes |
| `AZURE_TENANT_ID` | Azure tenant ID | Yes |
| `AZURE_CLIENT_ID` | Service principal client ID | Yes |
| `AZURE_CLIENT_SECRET` | Service principal secret | Yes |
| `POSTGRES_URL` | PostgreSQL connection string | Yes |
| `POSTGRES_USER` | PostgreSQL username | Yes |
| `POSTGRES_PASSWORD` | PostgreSQL password | Yes |
| `REDIS_HOST` | Redis host | Yes |
| `REDIS_PORT` | Redis port | Yes |
| `OAUTH2_ISSUER_URI` | OAuth2 issuer URI | Yes |
| `AZURE_APP_CONFIG_ENDPOINT` | Azure App Configuration endpoint | No |
| `AZURE_KEY_VAULT_ENABLED` | Enable Key Vault integration | No |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | OpenTelemetry endpoint | No |

### Application Properties

See `src/main/resources/application.yml` for full configuration options.

## Features

### Rule Management

- **Create Rules**: Define CDN/Front Door rules with match conditions and actions
- **Validation**: Automatic validation of rule configurations before application
- **Dry-Run**: Test rule application without making changes
- **Apply Rules**: Apply validated rules to Azure Front Door
- **Drift Detection**: Detect when Azure configuration differs from expected state

### Cache Purging

- **Single Path**: Purge specific content paths
- **Wildcard**: Purge paths matching patterns
- **Full Purge**: Purge all cache for a profile

### Resilience

- **Circuit Breaker**: Prevents cascading failures
- **Retry**: Automatic retry with exponential backoff
- **Timeouts**: Configurable timeouts for Azure API calls
- **Rate Limiting**: Prevent Azure API throttling

### Observability

- **OpenTelemetry**: Distributed tracing
- **Metrics**: Prometheus metrics export
- **Logging**: Structured logging with correlation IDs
- **Health Checks**: Kubernetes liveness/readiness probes

## Testing

```bash
# Run all tests
make test

# Run with coverage
make coverage

# Integration tests with Testcontainers
mvn verify -Pintegration-tests
```

## Development

### Code Style

```bash
# Check formatting
make lint

# Auto-format
make format
```

### Building

```bash
# Build
make build

# Build Docker image
make docker-build
```

## Azure API Management Policy

### JWT Validation

```xml
<policies>
  <inbound>
    <validate-jwt header-name="Authorization" failed-validation-httpcode="401">
      <openid-config url="https://login.microsoftonline.com/{tenant-id}/v2.0/.well-known/openid-configuration" />
      <audiences>
        <audience>{client-id}</audience>
      </audiences>
      <issuers>
        <issuer>https://login.microsoftonline.com/{tenant-id}/v2.0</issuer>
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

## Monitoring

### Health Checks

- Liveness: `/actuator/health/liveness`
- Readiness: `/actuator/health/readiness`
- Metrics: `/actuator/prometheus`

### Logs

Logs include correlation IDs for distributed tracing. Set `X-Correlation-ID` header in requests.

## License

Proprietary - Internal Use Only

