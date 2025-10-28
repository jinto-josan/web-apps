# Recommendations Service

A production-grade video recommendation microservice built with Java 17, Spring Boot 3.3.x, and Hexagonal Architecture.

## Overview

This service provides personalized video recommendations using a hybrid ranking approach with candidate generation, multi-factor scoring, and diversity constraints. It's designed for high availability, scalability, and observability.

### Key Features

- **Hexagonal Architecture**: Clear separation of domain, application, and infrastructure layers
- **Two-Stage Ranking**: Candidate generation → Multi-factor ranking → Diversity filtering
- **Feature Caching**: Redis-backed caching for user and video features
- **Resilience**: Circuit breakers, retries, timeouts, bulkheads, and rate limiting
- **Security**: OAuth2 Resource Server with Entra ID (Azure AD B2C) integration
- **Observability**: OpenTelemetry, metrics, traces, and logs
- **Azure Integration**: Cosmos DB, Redis, Service Bus, App Configuration, Key Vault

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     REST Controllers                       │
│              (GET /api/v1/recs/home, /next)               │
└────────────────────────┬───────────────────────────────────┘
                         │
┌────────────────────────▼───────────────────────────────────┐
│                  Application Layer                         │
│        (Use Cases, DTOs, Mappers)                          │
└────────────────────────┬───────────────────────────────────┘
                         │
┌────────────────────────▼───────────────────────────────────┐
│                    Domain Layer                             │
│    (Entities, Value Objects, Repository Ports)             │
└────────────────────────┬───────────────────────────────────┘
                         │
┌────────────────────────▼───────────────────────────────────┐
│                Infrastructure Layer                         │
│   (Adapters: JPA, Redis, Cosmos, Service Bus)             │
└─────────────────────────────────────────────────────────────┘
```

### Request Flow

1. **Request** → Controller validates input
2. **Candidate Generation** → Multiple providers fetch candidates
3. **Ranking** → Hybrid algorithm scores candidates (recency, relevance, popularity, diversity)
4. **Diversity Filtering** → Ensures category distribution
5. **Cache & Respond** → Cache features, return top N recommendations

## Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose (for local development)
- Kubernetes cluster (for production deployment)
- Azure subscription (for production)

## Local Development

### Quick Start

```bash
# Start infrastructure with emulators
make docker-up

# Run the service
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Run tests
mvn clean test
```

### Infrastructure Setup

The service requires:
- **PostgreSQL** (for user features)
- **Redis** (for feature caching)
- **Azure Cosmos DB Emulator** (optional)

Start them locally with:

```bash
# Using Docker Compose
docker-compose up -d

# Or manually
docker run -d --name postgres -p 5432:5432 -e POSTGRES_PASSWORD=postgres postgres:15
docker run -d --name redis -p 6379:6379 redis:7-alpine
```

### Environment Variables

```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/recommendations
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=postgres
export REDIS_HOST=localhost
export REDIS_PORT=6379
export JWT_ISSUER_URI=https://login.microsoftonline.com/{tenantId}/v2.0
```

## Running with Docker

### Build Image

```bash
docker build -t recommendations-service:latest .
```

### Run Container

```bash
docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/recommendations \
  -e REDIS_HOST=host.docker.internal \
  recommendations-service:latest
```

## Kubernetes Deployment

### Create Secrets

```bash
kubectl create secret generic recommendations-secrets \
  --from-literal=database-url='jdbc:postgresql://postgres:5432/recommendations' \
  --from-literal=database-username='postgres' \
  --from-literal=database-password='postgres' \
  --from-literal=cosmos-uri='https://...' \
  --from-literal=cosmos-key='...'
```

### Deploy with Helm

```bash
helm install recommendations-service ./charts/recommendations-service \
  --namespace production \
  --set image.repository=your-registry/recommendations-service \
  --set image.tag=1.0.0 \
  --set autoscaling.enabled=true
```

### Deploy with K8s Manifests

```bash
kubectl apply -f k8s/
```

## API Endpoints

### Get Home Recommendations

```bash
curl -X GET \
  "http://localhost:8080/api/v1/recs/home?userId=user123&limit=20" \
  -H "Authorization: Bearer {token}"
```

**Response:**
```json
{
  "recommendations": [
    {
      "videoId": "video123",
      "title": "Best Practices #1",
      "category": "Technology",
      "score": 0.85,
      "reason": "Highly relevant based on your interests",
      "recommendedAt": "2024-01-01T12:00:00Z",
      "additionalInfo": {
        "duration": 600
      }
    }
  ],
  "metadata": {
    "userId": "user123",
    "requestType": "home",
    "timestamp": "2024-01-01T12:00:00Z",
    "abTestVariant": "treatment",
    "totalCandidates": 100,
    "totalReturned": 20
  }
}
```

### Get Next-Up Recommendations

```bash
curl -X GET \
  "http://localhost:8080/api/v1/recs/next?userId=user123&videoId=video456&limit=10" \
  -H "Authorization: Bearer {token}"
```

## Configuration

### Application Properties

See `src/main/resources/application.yml` for configuration options:

- **Spring Data JPA**: PostgreSQL connection pooling, Flyway migrations
- **Redis**: Feature caching with TTL
- **Azure Cosmos**: Document storage for video candidates
- **Resilience4j**: Circuit breaker, retry, timeout, bulkhead, rate limiter
- **Security**: OAuth2 Resource Server, JWT validation
- **OpenAPI**: Springdoc for API documentation

### Resilience Patterns

```yaml
resilience4j:
  circuitbreaker:
    configs:
      default:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 30s
  retry:
    configs:
      default:
        maxAttempts: 3
        waitDuration: 500ms
```

## Testing

### Unit Tests

```bash
mvn test
```

### Integration Tests

```bash
# Requires Testcontainers (PostgreSQL, Redis)
mvn verify -Dspring.profiles.active=test
```

### Contract Tests

```bash
# WireMock for external service stubs
mvn test -Dtest=ContractTest
```

### Load Testing

A sample Gatling test is included in `src/test/scala/` for load testing scenarios.

## Observability

### Metrics

- Prometheus endpoint: `/actuator/prometheus`
- Custom metrics: `recommendations.requests.count`, `recommendations.cache.hits`

### Traces

OpenTelemetry automatically instruments HTTP requests, database queries, and external calls.

### Logs

Structured logging with correlation IDs for request tracing.

## Azure Integration

### Cosmos DB

Store video candidates and metadata:

```java
@Repository
public interface VideoCandidateCosmosRepository 
    extends CosmosRepository<VideoCandidateCosmosEntity, String> {
}
```

### Service Bus

Publish recommendation events for downstream processing:

```java
@Service
public class RecommendationEventPublisher {
    private final ServiceBusMessageSender messageSender;
    
    public void publish(RecommendationEvent event) {
        messageSender.send(event);
    }
}
```

### App Configuration & Key Vault

Centralized configuration and secrets management:

```yaml
spring:
  cloud:
    azure:
      appconfiguration:
        enabled: true
      keyvault:
        enabled: true
```

## Security

### API Security

- **OAuth2 Resource Server**: Validates JWT tokens from Entra ID
- **Rate Limiting**: Per-user rate limits via Resilience4j
- **CORS**: Configured for allowed origins

### Example APIM Policy

```xml
<validate-jwt header-name="Authorization" failed-validation-httpcode="401">
    <openid-config url="https://login.microsoftonline.com/{tenantId}/v2.0/.well-known/openid-configuration" />
</validate-jwt>

<rate-limit-by-key calls="100" renewal-period="60" counter-key="@(context.Request.Headers.GetValueOrDefault("Authorization", "").AsJwt()?.Subject)" />
```

## Monitoring & Alerts

### Health Checks

- Liveness: `/actuator/health/liveness`
- Readiness: `/actuator/health/readiness`
- Startup: `/actuator/health/startup`

### Alerts (Azure Monitor)

- High CPU/Memory usage
- Circuit breaker open for >5 minutes
- P95 latency >500ms
- Error rate >5%

## Troubleshooting

### Cache Misses

```bash
# Check Redis connectivity
redis-cli -h localhost ping

# View cache keys
redis-cli -h localhost KEYS "features:*"
```

### Database Issues

```bash
# Check connection
psql -h localhost -U postgres -d recommendations

# View tables
\dt
```

## Contributing

1. Create a feature branch
2. Write tests for your changes
3. Run `mvn clean verify`
4. Submit a pull request

## License

See [LICENSE](../LICENSE) file.

