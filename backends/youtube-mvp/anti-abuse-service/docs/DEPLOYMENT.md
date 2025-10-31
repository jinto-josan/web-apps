# Deployment Guide

## Prerequisites

1. **Azure Resources**:
   - Cosmos DB account with database `anti-abuse`
   - Azure ML online endpoint
   - Redis instance (or Azure Cache for Redis)
   - Key Vault for secrets
   - Event Hubs namespace (optional, for streaming)
   - Entra ID (Azure AD) app registration for OAuth2

2. **Container Registry**:
   - Azure Container Registry (ACR) or other registry

3. **Kubernetes Cluster**:
   - Azure Kubernetes Service (AKS) or other K8s cluster

## Build and Push Docker Image

```bash
# Build
docker build -t anti-abuse-service:0.1.0 .

# Tag for registry
docker tag anti-abuse-service:0.1.0 <registry>/anti-abuse-service:0.1.0

# Push
docker push <registry>/anti-abuse-service:0.1.0
```

## Deploy with Helm

### 1. Create Secrets

```bash
kubectl create secret generic anti-abuse-service-secrets \
  --from-literal=cosmos-endpoint='<cosmos-endpoint>' \
  --from-literal=cosmos-key='<cosmos-key>' \
  --from-literal=ml-endpoint-url='<ml-endpoint-url>' \
  --from-literal=ml-api-key='<ml-api-key>' \
  --from-literal=oauth2-issuer-uri='<issuer-uri>'
```

### 2. Install Helm Chart

```bash
helm install anti-abuse-service charts/anti-abuse-service \
  --set image.repository=<registry>/anti-abuse-service \
  --set image.tag=0.1.0 \
  --set secrets.cosmos.endpoint='<cosmos-endpoint>' \
  --set secrets.cosmos.key='<cosmos-key>' \
  --set secrets.ml.endpointUrl='<ml-endpoint-url>' \
  --set secrets.ml.apiKey='<ml-api-key>' \
  --set secrets.oauth2.issuerUri='<issuer-uri>'
```

### 3. Verify Deployment

```bash
kubectl get pods -l app.kubernetes.io/name=anti-abuse-service
kubectl logs -f <pod-name>
```

## Environment Variables

Required environment variables for production:

| Variable | Description |
|----------|-------------|
| `AZURE_COSMOS_ENDPOINT` | Cosmos DB endpoint |
| `AZURE_COSMOS_KEY` | Cosmos DB key |
| `AZURE_COSMOS_DB` | Database name (default: `anti-abuse`) |
| `AZURE_ML_ENDPOINT_URL` | ML endpoint URL |
| `AZURE_ML_API_KEY` | ML endpoint API key |
| `REDIS_HOST` | Redis host |
| `REDIS_PORT` | Redis port (default: 6379) |
| `OAUTH2_ISSUER_URI` | OAuth2 issuer URI |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | OpenTelemetry endpoint |

## Scaling

The service supports horizontal pod autoscaling:

```bash
# View HPA
kubectl get hpa anti-abuse-service-hpa

# Manual scaling
kubectl scale deployment anti-abuse-service --replicas=5
```

## Monitoring

- Metrics: `GET /actuator/metrics`
- Prometheus: `GET /actuator/prometheus`
- Health: `GET /actuator/health`
- Traces: OpenTelemetry to Azure Monitor

## Rollback

```bash
# Rollback Helm release
helm rollback anti-abuse-service

# Or rollback to specific revision
helm rollback anti-abuse-service <revision>
```

