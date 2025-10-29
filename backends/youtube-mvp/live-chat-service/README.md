# Live Chat Service

Java 17, Spring Boot 3.3.x, Hexagonal architecture. Real-time chat for live streams with slow mode and moderation. Azure Cosmos DB, Redis, Service Bus, Web PubSub.

## Run locally

- Prereqs: JDK 17, Docker, Maven.
- Start dependencies (Postgres, Redis, Azurite):

```bash
make up-local
```

- Build and run:

```bash
./mvnw -q -DskipTests package
java -jar target/live-chat-service-*.jar --spring.profiles.active=local
```

OpenAPI: `http://localhost:8080/swagger-ui.html`

## Endpoints (v1)
- POST `/api/v1/live/{id}/chat/token` — issue Web PubSub token
- GET `/api/v1/live/{id}/chat/history?since=&limit=` — recent history
- POST `/api/v1/live/{id}/chat/messages` — send message (Idempotency-Key supported)

## Config
See `src/main/resources/application.yml`. Key env vars:
- `WEBPUBSUB_CONNECTION_STRING`, `COSMOS_ENDPOINT`, `COSMOS_KEY`, `SERVICEBUS_CONNECTION_STRING`
- `OIDC_ISSUER_URI`, `APPLICATIONINSIGHTS_CONNECTION_STRING`

## APIM policies

JWT validation (B2C/Entra External ID):
```xml
<validate-jwt header-name="Authorization" failed-validation-httpcode="401" require-signed-tokens="true" require-expiration-time="true" output-token-variable-name="jwt">
  <openid-config url="https://login.microsoftonline.com/{tenantid}/v2.0/.well-known/openid-configuration"/>
  <audiences>
    <audience>api://your-api-app-id</audience>
  </audiences>
</validate-jwt>
```

Rate limiting:
```xml
<rate-limit-by-key calls="100" renewal-period="60" increment-condition="@(context.Request.Method == 'POST')" counter-key="@(context.Request.IpAddress)" />
```

## Kubernetes
See `k8s/` for Deployment, Service, HPA, PDB, NetworkPolicy.

## Notes
- Persistence: Cosmos (PK = liveId). Redis: recent messages + idempotency.
- Messaging: Service Bus moderation events, Outbox table for reliability.
- Observability: OpenTelemetry to Azure Monitor via exporter.
