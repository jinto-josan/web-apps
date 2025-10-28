# Search Query Service

Production-grade microservice for full-text search and autocomplete functionality using Azure Cognitive Search.

## Architecture

- **Hexagonal Architecture**: Domain, Application, Infrastructure layers
- **DDD-lite**: Entities, Value Objects, Repository pattern
- **CQRS**: Separate read and write operations
- **Eventually Consistent**: Index updates via messaging
- **Azure Integration**: Cosmos DB (source), Azure Search (index), Service Bus (events)

## Tech Stack

- Java 17
- Spring Boot 3.3.x
- Maven
- Azure Cognitive Search
- Azure Cosmos DB (SQL API)
- Azure Service Bus
- Azure App Configuration
- Azure Key Vault
- OpenTelemetry + Azure Monitor
- Resilience4j (retry, circuit breaker, bulkhead, rate limiter)
- Spring Security OAuth2 Resource Server

## Features

- Full-text search with filters (category, language, duration, views, date range)
- Autocomplete suggestions
- Pagination support
- ETag/If-None-Match caching
- API versioning (`/api/v1/`)
- Idempotent index updates
- Rate limit handling (429 retries)
- Eventually consistent search index

## Prerequisites

- JDK 17+
- Maven 3.9+
- Docker & Docker Compose (for local development)
- Azure account with:
  - Azure Cognitive Search service
  - Cosmos DB account
  - Service Bus namespace
  - App Configuration
  - Key Vault

## Local Development

### Running with Emulators

1. Start emulators:
```bash
make local-start
```

2. Run the service:
```bash
make run-local
```

### Running with Docker

```bash
docker build -t search-query-service .
docker run -p 8080:8080 \
  -e AZURE_COSMOS_ENDPOINT=http://host.docker.internal:8081 \
  -e AZURE_SEARCH_ENDPOINT=http://localhost:8080 \
  search-query-service
```

## Environment Variables

```bash
# Azure Cosmos DB
AZURE_COSMOS_ENDPOINT=https://youtube-mvp.documents.azure.com:443/
AZURE_COSMOS_KEY=<key>
AZURE_COSMOS_DATABASE=youtube-mvp
AZURE_COSMOS_CONTAINER=videos

# Azure Cognitive Search
AZURE_SEARCH_ENDPOINT=https://youtube-mvp.search.windows.net
AZURE_SEARCH_API_KEY=<key>
AZURE_SEARCH_INDEX_NAME=video-search-index

# Azure Service Bus
AZURE_SERVICEBUS_CONNECTION_STRING=<connection-string>
AZURE_SERVICEBUS_TOPIC=video-events
AZURE_SERVICEBUS_SUBSCRIPTION=search-index-updates

# Azure App Configuration
AZURE_APPCONFIG_ENDPOINT=https://appconfig-youtube-mvp.azconfig.io

# Azure Key Vault
AZURE_KEYVAULT_ENDPOINT=https://youtube-mvp-kv.vault.azure.net/

# OAuth2 / OIDC
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=https://youtube-mvp.b2clogin.com/youtube-mvp.onmicrosoft.com/b2c_1_signin/v2.0/
```

## API Endpoints

### Search
```http
GET /api/v1/search?query=java+tutorial&page=1&pageSize=20&category=programming

Response Headers:
ETag: "abc123"
If-None-Match: "abc123"
```

### Autocomplete
```http
GET /api/v1/suggest?prefix=java&maxResults=10
```

### Rebuild Index (Admin)
```http
POST /api/v1/index/rebuild
```

## Deployment

### Helm

```bash
helm install search-query-service ./charts/search-query-service \
  --set azure.cosmos.endpoint=<endpoint> \
  --set azure.search.endpoint=<endpoint>
```

### Kubernetes (Direct)

```bash
kubectl apply -f k8s/
```

## Testing

```bash
# Unit tests
make test

# Integration tests (with Testcontainers)
make integration-test

# Run all tests
make test-all
```

## Monitoring

- Health: `/actuator/health`
- Metrics: `/actuator/metrics`
- Prometheus: `/actuator/prometheus`
- Swagger UI: `/swagger-ui.html`

## APIM Policy Snippets

### JWT Validation
```xml
<validate-jwt header-name="Authorization">
    <openid-config url="https://youtube-mvp.b2clogin.com/youtube-mvp.onmicrosoft.com/b2c_1_signin/v2.0/.well-known/openid-configuration"/>
    <audiences>
        <audience>api://youtube-mvp</audience>
    </audiences>
</validate-jwt>
```

### Rate Limiting
```xml
<rate-limit-by-key calls="100" renewal-period="60" counter-key="@(context.Request.IpAddress)"/>
```

### CORS
```xml
<cors allow-credentials="true">
    <allowed-origins>
        <origin>https://www.youtube-mvp.com</origin>
    </allowed-origins>
    <allowed-methods>
        <method>GET</method>
        <method>POST</method>
    </allowed-methods>
</cors>
```

## Architecture Diagrams

See `docs/lld.md` for detailed low-level design and `docs/sequences/` for sequence diagrams.

## License

MIT
