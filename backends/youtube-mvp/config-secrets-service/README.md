# Configuration and Secrets Service

Production-grade microservice for central configuration and secrets management with multitenant support. This service provides a unified interface for managing application configurations and secrets, with read-through caching to Azure App Configuration and Azure Key Vault.

## Features

- **Multitenant Configuration Management**: Scope-based configuration with tenant isolation
- **Secrets Management**: Secure secret storage and rotation via Azure Key Vault
- **Read-Through Caching**: Redis-backed caching for improved performance
- **RBAC**: Role-based access control via JWT claims
- **Audit Logging**: Comprehensive audit trail for all operations
- **Event Publishing**: Service Bus integration for configuration change events
- **Resilience**: Circuit breakers, retries, and timeouts via Resilience4j
- **Observability**: OpenTelemetry integration with Azure Monitor
- **API Versioning**: RESTful API with `/api/v1/` versioning
- **ETag Support**: Optimistic concurrency control with ETags

## Architecture

This service follows **Hexagonal Architecture** (Ports & Adapters) with clear separation of concerns:

- **Domain Layer**: Entities, value objects, and port interfaces
- **Application Layer**: Use cases, DTOs, and mappers (MapStruct)
- **Infrastructure Layer**: Adapters for Azure services, Redis, JPA
- **Interface Layer**: REST controllers with OpenAPI documentation

## Technology Stack

- **Java 17** + **Spring Boot 3.3.x** + **Maven**
- **PostgreSQL**: Local repository for configuration metadata
- **Redis**: Caching layer
- **Azure App Configuration**: Configuration store
- **Azure Key Vault**: Secrets store
- **Azure Service Bus**: Event publishing
- **Spring Security OAuth2**: JWT-based authentication
- **Resilience4j**: Resilience patterns
- **OpenTelemetry**: Distributed tracing
- **Testcontainers**: Integration testing

## Prerequisites

- Java 17+
- Maven 3.9+
- Docker and Docker Compose (for local development)
- Azure subscription (for production deployment)
- PostgreSQL 15+ (or use Docker)
- Redis 7+ (or use Docker)

## Local Development

### Running with Docker Compose

```bash
# Start PostgreSQL and Redis
docker-compose up -d

# Run the service
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### Running Tests

```bash
# Unit tests
mvn test

# Integration tests (requires Docker)
mvn verify
```

### Environment Variables

Create a `.env` file or set the following environment variables:

```bash
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/configsecrets
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# Azure (for local, use mock or emulator)
AZURE_APP_CONFIG_ENDPOINT=http://localhost:8080/appconfig
AZURE_KEY_VAULT_URI=https://your-keyvault.vault.azure.net/
AZURE_SERVICE_BUS_CONNECTION_STRING=your-connection-string

# Security
JWT_ISSUER_URI=https://login.microsoftonline.com/{tenantId}/v2.0
JWT_JWK_SET_URI=https://login.microsoftonline.com/{tenantId}/discovery/v2.0/keys
```

## API Documentation

Once the service is running, access the Swagger UI at:
- http://localhost:8080/swagger-ui.html

### Example API Calls

#### Get Configuration

```bash
curl -X GET "http://localhost:8080/api/v1/config/tenant1/app.setting" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "If-None-Match: etag123"
```

#### Update Configuration

```bash
curl -X PUT "http://localhost:8080/api/v1/config/tenant1/app.setting" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -H "If-Match: etag123" \
  -d '{
    "value": "new-value",
    "content-type": "text/plain",
    "label": "production"
  }'
```

#### Rotate Secret

```bash
curl -X POST "http://localhost:8080/api/v1/secrets/tenant1/db.password/rotate" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "dry-run": false
  }'
```

## Docker

### Build Docker Image

```bash
docker build -t config-secrets-service:latest .
```

### Run Docker Container

```bash
docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/configsecrets \
  -e REDIS_HOST=host.docker.internal \
  config-secrets-service:latest
```

## Kubernetes Deployment

### Using Helm

```bash
# Install Helm chart
helm install config-secrets-service ./charts/config-secrets-service \
  --set secrets.databaseUrl="jdbc:postgresql://postgres:5432/configsecrets" \
  --set secrets.appConfigEndpoint="https://your-appconfig.azconfig.io" \
  --set secrets.keyVaultUri="https://your-keyvault.vault.azure.net/"
```

### Using kubectl

```bash
# Apply manifests
kubectl apply -f k8s/

# Create secrets
kubectl create secret generic config-secrets-secrets \
  --from-literal=database-url="jdbc:postgresql://postgres:5432/configsecrets" \
  --from-literal=database-username="postgres" \
  --from-literal=database-password="your-password" \
  --from-literal=app-config-endpoint="https://your-appconfig.azconfig.io" \
  --from-literal=key-vault-uri="https://your-keyvault.vault.azure.net/" \
  --from-literal=service-bus-connection-string="your-connection-string"
```

## Azure API Management (APIM) Policy Snippets

### JWT Validation

```xml
<validate-jwt header-name="Authorization" failed-validation-httpcode="401" failed-validation-error-message="Unauthorized">
    <openid-config url="https://login.microsoftonline.com/{tenantId}/.well-known/openid-configuration" />
    <required-claims>
        <claim name="aud" match="any">
            <value>your-client-id</value>
        </claim>
    </required-claims>
</validate-jwt>
```

### Rate Limiting

```xml
<rate-limit-by-key calls="100" renewal-period="60" counter-key="@(context.Request.Headers.GetValueOrDefault("Authorization","").AsJwt()?.Claims.GetValueOrDefault("sub","anonymous"))" />
```

## RBAC Configuration

The service checks JWT claims for permissions:

- **Scopes**: `config.read`, `config.write`, `config.secret.rotate`, `config.admin`
- **Roles**: `config-admin`, `config-reader`, `config-writer`
- **Tenant Isolation**: Users can only access configurations in their tenant (unless admin)

Example JWT claims:

```json
{
  "sub": "user123",
  "tid": "tenant1",
  "scp": "config.read config.write",
  "roles": ["config-reader"]
}
```

## Observability

### Metrics

- Prometheus metrics available at `/actuator/prometheus`
- Key metrics: `http_server_requests_seconds`, `jvm_memory_used_bytes`, `cache_gets_total`

### Tracing

- OpenTelemetry traces exported to Azure Monitor
- Correlation IDs propagated via HTTP headers

### Logging

- Structured JSON logging in production
- Log levels: `DEBUG` (local), `INFO` (production)

## Security

- **Authentication**: OAuth2 Resource Server with JWT validation
- **Authorization**: RBAC via JWT claims
- **Secrets**: Stored in Azure Key Vault, never in local database
- **Audit**: All operations logged with user, tenant, and timestamp
- **Network**: Network policies restrict pod communication

## Performance

- **Caching**: 5-minute TTL for configuration reads
- **Connection Pooling**: HikariCP with optimized settings
- **Circuit Breakers**: Protect against downstream failures
- **Retries**: Exponential backoff for transient failures

## Troubleshooting

### Service won't start

1. Check database connectivity: `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`
2. Check Redis connectivity: `REDIS_HOST`, `REDIS_PORT`
3. Check Azure credentials: Ensure managed identity or service principal is configured

### Configuration not found

1. Verify scope and key are correct
2. Check RBAC permissions in JWT token
3. Verify tenant matches (`tid` claim)

### Secret rotation fails

1. Check Key Vault permissions (Secret rotation requires `Key Vault Secrets Officer` role)
2. Verify secret exists in Key Vault
3. Check audit logs for detailed error messages

## License

See LICENSE file in project root.

