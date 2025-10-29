## Notifications Service (youtube-mvp)

### Purpose
Push/email/in-app notifications with user preferences. Azure integrations: Notification Hubs, SendGrid, Cosmos DB, Service Bus.

### Endpoints
- POST `/api/v1/notifications/test` — enqueue a test notification job
- GET `/api/v1/users/{id}/notification-prefs` — fetch preferences (supports ETag)
- PUT `/api/v1/users/{id}/notification-prefs` — upsert preferences

### Run locally
Prereqs: Java 17, Maven, Docker (for Redis). Cosmos emulator optional.

```bash
make build
docker run -p 6379:6379 -d redis:7-alpine
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### Docker
```bash
make docker IMAGE=notifications-service:local
```

### Kubernetes (Helm)
```bash
helm upgrade -i notifications-service ./charts/notification-service \
  --set image.repository=yourrepo/notifications-service \
  --set image.tag=0.0.1
```

### Configuration
- `application.yml` contains Azure settings (Cosmos, Service Bus). Use `application-local.yml` for emulators.
- Bootstrap for App Configuration/Key Vault can be enabled via env and setting appropriate endpoints.

### Security
Resource Server with OIDC (Entra External ID/B2C). Set `SECURITY_OIDC_ISSUER_URI`.

### Observability
OpenTelemetry Java Agent and Azure Monitor exporter included. Provide OTLP/connection settings as env vars.

### APIM policies (snippets)
JWT validation (replace issuer and audience):
```xml
<validate-jwt header-name="Authorization" failed-validation-httpcode="401" require-expiration-time="true" require-scheme="Bearer">
  <openid-config url="https://login.microsoftonline.com/<tenant>/v2.0/.well-known/openid-configuration" />
  <audiences>
    <audience>api://your-app-id</audience>
  </audiences>
  <issuers>
    <issuer>https://login.microsoftonline.com/<tenant>/v2.0</issuer>
  </issuers>
</validate-jwt>
```

Rate limit:
```xml
<rate-limit calls="60" renewal-period="60" />
```

### Architecture
- Hexagonal: domain, application, infrastructure
- Messaging: Service Bus topic for jobs; idempotent consumer (Redis) recommended
- Providers: SendGrid (email), Notification Hubs (push)
- Storage: Cosmos DB for preferences; optional SQL outbox

### Tests
- Unit tests for services/mappers
- MVC slice tests for controllers
- Integration with Testcontainers (Redis) can be enabled as needed


