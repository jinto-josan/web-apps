package com.youtube.common.domain.web;

import com.youtube.common.domain.services.tracing.TraceProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Auto-configuration for common-domain web components.
 * Automatically registers CorrelationFilter and HTTP client configurations.
 * 
 * <p>This configuration is only active for servlet-based web applications.</p>
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class CommonDomainWebAutoConfiguration {
    
    /**
     * Creates the CorrelationFilter bean.
     * 
     * @param traceProvider the trace provider
     * @return configured CorrelationFilter
     */
    @Bean
    public CorrelationFilter correlationFilter(TraceProvider traceProvider) {
        return new CorrelationFilter(traceProvider);
    }
    
    /**
     * Registers CorrelationFilter in the servlet filter chain.
     * 
     * @param filter the CorrelationFilter bean
     * @return FilterRegistrationBean with highest precedence
     */
    @Bean
    public FilterRegistrationBean<CorrelationFilter> correlationFilterRegistration(
            CorrelationFilter filter) {
        FilterRegistrationBean<CorrelationFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(filter);
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE); // Run first to set correlation context
        return reg;
    }
}

