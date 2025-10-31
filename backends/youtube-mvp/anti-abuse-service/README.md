# Anti-Abuse Service

Production-grade microservice for real-time risk scoring and fraud detection using **Java 17**, **Spring Boot 3.3+**, **Azure ML**, **Cosmos DB**, **Redis**, and **Event Hubs**.

## Features

- **Real-Time Risk Scoring** - Calculate risk scores for views, ads, comments, uploads using ML and rules
- **Rule Engine** - DSL-based rule evaluation with AND/OR conditions and multiple operators
- **Feature Enrichment** - Combine real-time features with historical feature store data
- **ML Integration** - Azure ML online endpoint for predictive risk scoring
- **Shadow Evaluation** - Run evaluations without enforcement for model testing
- **Resilience** - Circuit breaker, retry, and timeout for ML endpoint calls
- **Observability** - OpenTelemetry integration with Azure Monitor

## Architecture

### Hexagonal Architecture

```
┌─────────────────────────────────────────────────────┐
│              Infrastructure Layer                    │
│   Controllers | Filters | Config | Exception Handlers│
└─────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────┐
│               Application Layer                      │
│      Services | DTOs | Mappers                       │
└─────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────┐
│                  Domain Layer                        │
│    Entities | Value Objects | Repository Interfaces │
│    Domain Services (RiskEngine, RuleEvaluator)      │
└─────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────┐
│              Infrastructure Layer                    │
│  Cosmos DB | Redis | ML Endpoint | Event Hubs      │
└─────────────────────────────────────────────────────┘
```

### Data Model

**Rules** (Cosmos DB):
- Partition Key: `name`
- Container: `rules`
- Stores fraud detection rules with conditions

**Feature Store** (Cosmos DB):
- Partition Key: `userId`
- Container: `feature-store`
- Stores historical user features

**Risk Scores** (Event Hubs):
- Streams risk scores for downstream processing
- Enables real-time analytics and alerting

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose (for local emulators)
- Azure CLI (for cloud deployment)
- Azure ML endpoint (for risk prediction)

### Local Development

```bash
# Start Redis
docker run -d -p 6379:6379 redis:7-alpine

# Build
mvn clean package -DskipTests

# Run
java -jar target/anti-abuse-service-0.1.0-SNAPSHOT.jar --spring.profiles.active=local
```

The service will start on `http://localhost:8080`.

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `AZURE_COSMOS_ENDPOINT` | Cosmos DB endpoint | - |
| `AZURE_COSMOS_KEY` | Cosmos DB key | - |
| `AZURE_COSMOS_DB` | Database name | `anti-abuse` |
| `AZURE_ML_ENDPOINT_URL` | ML endpoint URL | - |
| `AZURE_ML_API_KEY` | ML endpoint API key | - |
| `REDIS_HOST` | Redis host | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `AZURE_EVENT_HUBS_CONNECTION_STRING` | Event Hubs connection | - |
| `OAUTH2_ISSUER_URI` | OAuth2 issuer | - |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | OpenTelemetry endpoint | `http://localhost:4318` |

### Docker

```bash
# Build image
docker build -t anti-abuse-service:latest .

# Run
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=local \
  -e REDIS_HOST=host.docker.internal \
  anti-abuse-service:latest
```

### Kubernetes

```bash
# Deploy
kubectl apply -f k8s/

# Or use Helm
helm install anti-abuse-service charts/anti-abuse-service
```

## API Endpoints

### Calculate Risk Score

```bash
POST /api/v1/risk/score
Content-Type: application/json

{
  "eventType": "VIEW",
  "userId": "user123",
  "contentId": "video456",
  "context": {
    "ipAddress": "192.168.1.1",
    "userAgent": "Mozilla/5.0",
    "region": "US"
  }
}

Response:
{
  "eventId": "01HZ...",
  "score": 0.75,
  "riskLevel": "HIGH",
  "triggeredRules": ["rule-1", "rule-3"],
  "recommendedAction": "REVIEW",
  "latencyMs": 45,
  "metadata": {}
}
```

### Evaluate Rules

```bash
POST /api/v1/rules/evaluate
Content-Type: application/json

{
  "userId": "user123",
  "features": {
    "totalEvents": 150,
    "riskHistory": 0.6
  },
  "context": {
    "region": "US"
  }
}

Response:
{
  "triggeredRules": ["rule-1"],
  "recommendedAction": "BLOCK",
  "metadata": {
    "userId": "user123"
  }
}
```

## Risk Scoring Algorithm

1. **Feature Enrichment**: Combine event context with historical features
2. **Rule Evaluation**: Evaluate all enabled rules against features
3. **ML Prediction**: Call Azure ML endpoint for risk score
4. **Score Combination**: Weighted combination (70% ML, 30% rules)
5. **Risk Level**: Map score to LOW/MEDIUM/HIGH/CRITICAL
6. **Action**: Determine enforcement action based on triggered rules

## Rule DSL

Rules support:
- **Operators**: AND, OR
- **Predicates**: GT, LT, EQ, NOT_EQ, IN, NOT_IN
- **Fields**: Any feature field
- **Priority**: Higher priority rules take precedence

Example rule:
```json
{
  "id": "rule-1",
  "name": "High Event Count",
  "condition": {
    "operator": "AND",
    "predicates": [
      {
        "field": "totalEvents",
        "operator": "GT",
        "value": 100
      },
      {
        "field": "riskHistory",
        "operator": "GT",
        "value": 0.5
      }
    ]
  },
  "action": "BLOCK",
  "priority": 100,
  "enabled": true
}
```

## Resilience

- **Circuit Breaker**: Opens after 50% failure rate (20 request window)
- **Retry**: 2 attempts with exponential backoff for ML endpoint
- **Timeout**: 2s timeout for ML endpoint calls
- **Fallback**: Returns 0.0 risk score if ML endpoint unavailable

## Observability

- **Traces**: OpenTelemetry auto-instrumentation
- **Metrics**: Spring Actuator + Prometheus
- **Logs**: Structured logging with correlation IDs
- **Latency**: P95 < 100ms for risk scoring

## Health Checks

The service exposes health check endpoints:

- `GET /actuator/health` - Overall health
- `GET /actuator/health/liveness` - Liveness probe (Kubernetes)
- `GET /actuator/health/readiness` - Readiness probe (Kubernetes)

Health checks verify:
- Redis connectivity
- Cosmos DB connectivity
- ML endpoint availability (indirect via circuit breaker state)

## Testing

```bash
# Unit tests
mvn test

# Integration tests (requires Testcontainers)
mvn verify

# Coverage report
mvn jacoco:report
```

## Documentation

- [Low-Level Design](./docs/lld.md)
- [Sequence Diagrams](./docs/sequences/)

## License

MIT

