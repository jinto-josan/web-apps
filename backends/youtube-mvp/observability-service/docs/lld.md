# Low-Level Design: Observability & SRE Tools Service

## Overview

This document describes the low-level design of the Observability & SRE Tools Service, implementing SLO management, synthetic checks, and metrics export capabilities.

## Architecture Layers

### Domain Layer

#### Entities

**SLO (Service Level Objective)**
- `id`: SLOId (UUID)
- `name`: String
- `serviceName`: String
- `description`: String
- `slis`: List<SLI>
- `targetPercent`: Double (e.g., 99.9)
- `timeWindow`: TimeWindow (rolling/calendar)
- `errorBudget`: Double (calculated)
- `errorBudgetRemaining`: Double (calculated)
- `labels`: Map<String, String>

**SLI (Service Level Indicator)**
- `name`: String
- `type`: SLIType (AVAILABILITY, LATENCY, ERROR_RATE, THROUGHPUT, CUSTOM)
- `query`: String (KQL query for Azure Monitor)
- `lastCalculatedAt`: Instant
- `lastValue`: Double (0-100)

**SyntheticCheck**
- `id`: SyntheticCheckId (UUID)
- `name`: String
- `description`: String
- `type`: SyntheticCheckType (HTTP, HTTPS, TCP, DNS, SSL_CERT, MULTI_STEP)
- `endpoint`: String
- `method`: String
- `headers`: Map<String, String>
- `body`: String (for POST)
- `expectedStatusCode`: Integer
- `expectedBodyPattern`: String (regex)
- `timeoutSeconds`: Integer
- `intervalSeconds`: Integer
- `enabled`: Boolean
- `lastRunAt`: Instant
- `lastResult`: SyntheticCheckResult

**SyntheticCheckResult**
- `executedAt`: Instant
- `success`: Boolean
- `statusCode`: Integer
- `responseTimeMs`: Long
- `responseBody`: String
- `errorMessage`: String
- `metadata`: Map<String, String>

#### Value Objects

- `SLOId`: UUID wrapper
- `SyntheticCheckId`: UUID wrapper
- `TimeWindow`: Duration + Type (ROLLING/CALENDAR)

#### Repository Ports

- `SLORepository`: save, findById, findByServiceName, findAll, deleteById
- `SyntheticCheckRepository`: save, findById, findAll, findByEnabled, deleteById

#### Domain Services (Ports)

- `SLOCalculator`: Calculate SLO from SLIs, error budget burn rate, remaining budget
- `AzureMonitorQueryPort`: Execute KQL queries, calculate SLI values
- `SyntheticCheckRunner`: Execute synthetic checks
- `SLOExporterPort`: Export SLO metrics to external systems

### Application Layer

#### Services

**SLOApplicationService**
- `createSLO(CreateSLORequest)`: Create new SLO
- `getSLO(String)`: Get SLO by ID with current calculations
- `getAllSLOs()`: List all SLOs
- `getSLOsByService(String)`: Filter by service name
- `recalculateSLO(String)`: Trigger recalculation

**SyntheticCheckApplicationService**
- `createCheck(CreateSyntheticCheckRequest)`: Create new check
- `getCheck(String)`: Get check by ID
- `getAllChecks()`: List all checks
- `runCheck(String)`: Manually execute check
- `enableCheck(String)`: Enable scheduled execution
- `disableCheck(String)`: Disable scheduled execution
- `deleteCheck(String)`: Delete check

#### DTOs

- `CreateSLORequest`, `SLOResponse`, `SLIResponse`
- `CreateSyntheticCheckRequest`, `SyntheticCheckResponse`, `SyntheticCheckResultResponse`

#### Mappers (MapStruct)

- `SLOMapper`: Domain ↔ DTO conversions
- `SyntheticCheckMapper`: Domain ↔ DTO conversions

### Infrastructure Layer

#### Persistence Adapters

**SLORepositoryAdapter** (JPA → Domain)
- Maps `SLOEntity` ↔ `SLO`
- Handles SLI embedding
- Manages labels as separate table

**SyntheticCheckRepositoryAdapter** (JPA → Domain)
- Maps `SyntheticCheckEntity` ↔ `SyntheticCheck`
- Embeds result in entity

#### Service Implementations

**DefaultSLOCalculator**
- Simple average of SLI values
- Error budget burn rate calculation
- Remaining budget calculation

**AzureMonitorQueryAdapter**
- Uses `LogsQueryClient` to execute KQL queries
- Extracts numeric results from query response
- Handles errors gracefully

**HttpSyntheticCheckRunner**
- Uses OkHttpClient for HTTP/HTTPS requests
- Validates status codes and response bodies
- Measures response time
- Captures error messages

**PrometheusSLOExporter**
- Exports SLO metrics via Micrometer
- Creates gauges for target, current, error budget
- Tags metrics with SLO metadata

#### Schedulers

**SLIRecalculatorScheduler**
- Runs every 5 minutes (configurable)
- Recalculates all SLOs
- Updates SLI values from Azure Monitor
- Exports metrics

**SyntheticCheckScheduler**
- Runs every 1 minute (configurable)
- Executes enabled checks based on interval
- Updates check results

### Interface Layer

#### REST Controllers

**SLOController** (`/api/v1/slo`)
- `POST /` - Create SLO
- `GET /{id}` - Get SLO
- `GET /` - List SLOs (filter by serviceName)
- `POST /{id}/recalculate` - Trigger recalculation

**SyntheticCheckController** (`/api/v1/synthetics`)
- `POST /` - Create check
- `GET /{id}` - Get check
- `GET /` - List checks
- `POST /{id}/run` - Run check
- `POST /{id}/enable` - Enable check
- `POST /{id}/disable` - Disable check
- `DELETE /{id}` - Delete check

**HealthzController** (`/api/v1/healthz`)
- `GET /deep` - Deep health check with dependencies
- `GET /` - Basic health check

## Database Schema

### Tables

**slos**
- Primary key: `id` (UUID)
- Indexes: `service_name`, `created_at`

**slis**
- Composite key: `(slo_id, name)`
- Foreign key: `slo_id` → `slos(id)`

**slo_labels**
- Composite key: `(slo_id, label_key)`
- Foreign key: `slo_id` → `slos(id)`

**synthetic_checks**
- Primary key: `id` (UUID)
- Indexes: `enabled`, `last_run_at`

**synthetic_check_headers**
- Composite key: `(check_id, header_key)`
- Foreign key: `check_id` → `synthetic_checks(id)`

**synthetic_check_labels**
- Composite key: `(check_id, label_key)`
- Foreign key: `check_id` → `synthetic_checks(id)`

## Flow Diagrams

### SLO Calculation Flow

```
User Request → SLOController → SLOApplicationService
                                    ↓
                            SLORepository.find()
                                    ↓
                    AzureMonitorQueryPort.calculateSLI()
                                    ↓
                        SLOCalculator.calculateSLO()
                                    ↓
                    SLOExporterPort.export()
                                    ↓
                            SLORepository.save()
                                    ↓
                            Response with current SLO
```

### Synthetic Check Execution Flow

```
Scheduler/User → SyntheticCheckApplicationService
                                    ↓
                    SyntheticCheckRunner.run()
                                    ↓
                        HttpSyntheticCheckRunner
                                    ↓
                        OkHttpClient.execute()
                                    ↓
                        Validate response
                                    ↓
                    SyntheticCheckRepository.save()
                                    ↓
                            Return result
```

### Metrics Export Flow

```
SLO Recalculation → PrometheusSLOExporter.export()
                                    ↓
                        MeterRegistry.gauge()
                                    ↓
                    Prometheus endpoint exposure
                                    ↓
                    Scraped by monitoring system
```

## Security

- OAuth2 Resource Server (JWT validation)
- Managed Identity for Azure services
- Network policies in Kubernetes
- Non-root container user

## Resilience

- Resilience4j:
  - Retry for Azure Monitor queries
  - Circuit breaker for external calls
  - Rate limiter for synthetic checks
- Timeouts configured for all HTTP calls
- Graceful error handling with ProblemDetails (RFC7807)

## Observability

- OpenTelemetry auto-instrumentation
- Correlation IDs in logs
- Azure Monitor integration
- Prometheus metrics export
- Structured logging

