/**
 * Infrastructure services for cross-cutting concerns.
 * 
 * <p>This package contains services that provide infrastructure capabilities
 * used across all microservices:
 * <ul>
 *   <li>{@link com.youtube.common.domain.services.correlation.CorrelationContext} - Correlation ID management</li>
 *   <li>{@link com.youtube.common.domain.services.tracing.TraceProvider} - Distributed tracing</li>
 *   <li>{@link com.youtube.common.domain.services.idempotency.IdempotencyService} - HTTP request idempotency</li>
 *   <li>{@link com.youtube.common.domain.services.featureflags.FeatureFlagService} - Feature flag evaluation</li>
 *   <li>{@link com.youtube.common.domain.services.tenant.TenantResolver} - Multi-tenant support</li>
 * </ul>
 */
package com.youtube.common.domain.services;

