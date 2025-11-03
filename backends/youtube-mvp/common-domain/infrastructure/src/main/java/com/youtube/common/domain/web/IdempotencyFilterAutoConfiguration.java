package com.youtube.common.domain.web;

import com.youtube.common.domain.persistence.idempotency.HttpIdempotencyRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Auto-configuration for HTTP idempotency filter.
 * 
 * <p>Automatically configures IdempotencyFilter when:
 * - Running in a servlet-based web application
 * - HttpIdempotencyRepository class is on classpath
 * - An HttpIdempotencyRepository bean is available (auto-configured by JPA or Redis)
 * </p>
 * 
 * <p>The filter is registered with order HIGHEST_PRECEDENCE + 1, so it runs
 * after CorrelationFilter but before other filters.</p>
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(HttpIdempotencyRepository.class)
@ConditionalOnBean(HttpIdempotencyRepository.class)
public class IdempotencyFilterAutoConfiguration {
    
    /**
     * Creates the IdempotencyFilter bean.
     * 
     * @param repository the idempotency repository (auto-configured)
     * @return configured IdempotencyFilter
     */
    @Bean
    @ConditionalOnMissingBean
    public IdempotencyFilter idempotencyFilter(HttpIdempotencyRepository repository) {
        return new IdempotencyFilter(repository);
    }
    
    /**
     * Registers IdempotencyFilter in the servlet filter chain.
     * 
     * @param filter the IdempotencyFilter bean
     * @return FilterRegistrationBean with order HIGHEST_PRECEDENCE + 1
     */
    @Bean
    public FilterRegistrationBean<IdempotencyFilter> idempotencyFilterRegistration(
            IdempotencyFilter filter) {
        FilterRegistrationBean<IdempotencyFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1); // After CorrelationFilter
        registration.addUrlPatterns("/*");
        return registration;
    }
}

