# Application Insights Configuration for Identity-Auth-Service

This document describes how Application Insights is configured for the identity-auth-service.

## Overview

The service is configured to send telemetry data to Azure Application Insights for:
- **Distributed Tracing**: Track requests across microservices
- **Metrics**: Performance metrics (response times, throughput, etc.)
- **Exceptions**: Error tracking and diagnostics
- **Dependencies**: Database, Redis, Service Bus calls

## Dependencies

The following dependencies are included:

1. **applicationinsights-runtime-attach** (v3.7.5): Application Insights Java agent for auto-instrumentation
2. **micrometer-registry-azure-monitor**: Micrometer registry for exporting metrics to Application Insights

## Configuration

### Environment Variables

Set the following environment variables:

```bash
# Required: Application Insights connection string
APPLICATIONINSIGHTS_CONNECTION_STRING="InstrumentationKey=xxx-xxx-xxx-xxx;IngestionEndpoint=https://xxxx.applicationinsights.azure.com/;LiveEndpoint=https://xxxx.livediagnostics.monitor.azure.com/"

# Optional: Enable/disable Application Insights (default: true)
APPLICATIONINSIGHTS_ENABLED=true

# Optional: Sampling percentage 0.0-1.0 (default: 1.0 = 100%)
APPLICATIONINSIGHTS_SAMPLING_PERCENTAGE=1.0

# Optional: Legacy instrumentation key (not recommended, use connection string)
APPLICATIONINSIGHTS_INSTRUMENTATION_KEY=xxx-xxx-xxx-xxx

# Optional: Log level for Application Insights (default: INFO)
APPLICATIONINSIGHTS_LOG_LEVEL=INFO
```

### Connection String

The connection string can be found in your Azure Application Insights resource:
1. Go to Azure Portal
2. Navigate to your Application Insights resource
3. Go to "Overview" → "Essentials" → "Connection String"
4. Copy the connection string

### Application Configuration

The configuration is defined in:
- `application.yml`: Base configuration with environment variable placeholders
- `application-docker.yml`: Docker-specific configuration

## Features Enabled

### Auto-Instrumentation

The Application Insights Java agent automatically instruments:
- **Spring Boot MVC**: HTTP requests, responses, exceptions
- **JPA/Hibernate**: Database queries and transactions
- **Redis**: Redis operations
- **Azure Service Bus**: Message publishing and consumption
- **HTTP Clients**: Outbound HTTP calls

### Distributed Tracing

The service uses Micrometer Tracing (via `TraceProvider` from common-domain) which is compatible with Application Insights:
- **Trace Context Propagation**: W3C TraceContext format (traceparent header)
- **Correlation IDs**: Custom correlation ID support
- **Span Creation**: Automatic span creation for requests

### Metrics Export

Micrometer metrics are exported to Application Insights:
- **Custom Metrics**: Service-specific metrics
- **JVM Metrics**: Memory, GC, thread metrics
- **Spring Boot Metrics**: Request rates, response times

### Sampling

Configure sampling percentage to control data volume:
- `1.0` = 100% of telemetry sent (recommended for development)
- `0.1` = 10% of telemetry sent (recommended for high-traffic production)

## Integration with Common-Domain

The service integrates with common-domain tracing infrastructure:

- **TraceProvider**: Uses Micrometer Tracer which is auto-configured by Application Insights
- **CorrelationFilter**: Sets correlation IDs and trace context
- **Outbox Events**: Events include traceparent header for end-to-end tracing

## Verification

After deployment, verify Application Insights is working:

1. **Check Logs**: Look for "Application Insights Agent started" in application logs
2. **Azure Portal**: Go to Application Insights → Live Metrics (should show live data)
3. **Application Map**: Should show the identity-auth-service with dependencies
4. **Traces**: Check "Transaction search" for incoming requests

## Troubleshooting

### Connection String Not Set

If the connection string is not set:
- Application Insights agent will log a warning
- Telemetry will not be sent
- Check environment variables or application.yml

### High Data Volume

If data volume is too high:
- Reduce `APPLICATIONINSIGHTS_SAMPLING_PERCENTAGE`
- Consider using Application Insights sampling filters
- Review custom metrics frequency

### Missing Traces

If traces are missing:
- Verify connection string is correct
- Check Application Insights resource region matches deployment region
- Verify network connectivity to Application Insights endpoints
- Check firewall rules

## References

- [Application Insights Java Documentation](https://learn.microsoft.com/en-us/azure/azure-monitor/app/java-spring-boot)
- [Micrometer Azure Monitor Registry](https://micrometer.io/docs/registry/azure-monitor)
- [Application Insights Connection String](https://learn.microsoft.com/en-us/azure/azure-monitor/app/sdk-connection-string)

