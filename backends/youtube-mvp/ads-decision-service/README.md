## Ads Decision Service

Real-time ad decisioning with pacing and frequency caps.

### Tech
- Java 17, Spring Boot 3.3.x
- Hexagonal architecture (domain, application, infrastructure, interfaces)
- Postgres (JPA/Flyway), Redis (caps), Cosmos (targeting)
- Azure Service Bus (campaign updates), Event Hubs (impressions)
- Spring Security Resource Server (OIDC), OpenAPI, Resilience4j, OpenTelemetry

### Run locally
```
make run
```

Profiles: `local` uses local Postgres, Redis, Cosmos emulator.

### Build & Docker
```
make build
make docker
```

### Kubernetes
Apply manifests in `k8s/` or install Helm chart in `charts/ads-decision-service`.

### API
- POST `/api/v1/ads/decision` body:
```json
{ "userId": "u1", "videoId": "v1", "context": {"country":"US"} }
```

### APIM policy snippets
- JWT validation
```xml
<validate-jwt header-name="Authorization" failed-validation-httpcode="401" failed-validation-error-message="Unauthorized">
  <openid-config url="https://login.microsoftonline.com/{tenant}/v2.0/.well-known/openid-configuration" />
  <audiences>
    <audience>api://your-app-id</audience>
  </audiences>
</validate-jwt>
```

- Rate limiting
```xml
<rate-limit-by-key calls="100" renewal-period="60" increment-condition="@(context.Request.Method == \"POST\")" counter-key="@(context.Subscription?.Key ?? context.Request.IpAddress)" />
```

### Observability
Traces/metrics/logs exported to Azure Monitor via OpenTelemetry exporter. Configure via environment variables.


