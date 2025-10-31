# Deployment Guide

## Prerequisites

1. **Azure Resources**:
   - Cosmos DB account with database `experimentation`
   - Azure App Configuration instance
   - Redis instance (or Azure Cache for Redis)
   - Key Vault for secrets
   - Entra ID (Azure AD) app registration for OAuth2

2. **Container Registry**:
   - Azure Container Registry (ACR) or other registry

3. **Kubernetes Cluster**:
   - Azure Kubernetes Service (AKS) or other K8s cluster

## Build and Push Docker Image

```bash
# Build
docker build -t experimentation-service:0.1.0 .

# Tag for registry
docker tag experimentation-service:0.1.0 <registry>/experimentation-service:0.1.0

# Push
docker push <registry>/experimentation-service:0.1.0
```

## Deploy with Helm

### 1. Create Secrets

```bash
kubectl create secret generic experimentation-service-secrets \
  --from-literal=cosmos-endpoint='<cosmos-endpoint>' \
  --from-literal=cosmos-key='<cosmos-key>' \
  --from-literal=oauth2-issuer-uri='<issuer-uri>'
```

### 2. Install Helm Chart

```bash
helm install experimentation-service charts/experimentation-service \
  --set image.repository=<registry>/experimentation-service \
  --set image.tag=0.1.0 \
  --set secrets.cosmos.endpoint='<cosmos-endpoint>' \
  --set secrets.cosmos.key='<cosmos-key>' \
  --set secrets.oauth2.issuerUri='<issuer-uri>'
```

### 3. Verify Deployment

```bash
kubectl get pods -l app.kubernetes.io/name=experimentation-service
kubectl logs -f <pod-name>
```

## Environment Variables

Required environment variables for production:

| Variable | Description |
|----------|-------------|
| `AZURE_COSMOS_ENDPOINT` | Cosmos DB endpoint |
| `AZURE_COSMOS_KEY` | Cosmos DB key |
| `AZURE_COSMOS_DB` | Database name (default: `experimentation`) |
| `AZURE_APP_CONFIG_ENDPOINT` | App Configuration endpoint |
| `AZURE_APP_CONFIG_CONNECTION_STRING` | App Configuration connection string |
| `REDIS_HOST` | Redis host |
| `REDIS_PORT` | Redis port (default: 6379) |
| `OAUTH2_ISSUER_URI` | OAuth2 issuer URI |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | OpenTelemetry endpoint |

## Scaling

The service supports horizontal pod autoscaling:

```bash
# View HPA
kubectl get hpa experimentation-service-hpa

# Manual scaling
kubectl scale deployment experimentation-service --replicas=5
```

## Monitoring

- Metrics: `GET /actuator/metrics`
- Prometheus: `GET /actuator/prometheus`
- Health: `GET /actuator/health`
- Traces: OpenTelemetry to Azure Monitor

## Rollback

```bash
# Rollback Helm release
helm rollback experimentation-service

# Or rollback to specific revision
helm rollback experimentation-service <revision>
```

