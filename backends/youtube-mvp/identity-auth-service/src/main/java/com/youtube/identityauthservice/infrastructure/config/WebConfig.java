package com.youtube.identityauthservice.infrastructure.config;

import com.youtube.identityauthservice.infrastructure.persistence.HttpIdempotencyRepository;
import com.youtube.identityauthservice.infrastructure.util.IdempotencyFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration for CORS and other web-related settings.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/auth/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
                
        registry.addMapping("/.well-known/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }

    @Bean
    public FilterRegistrationBean<CorrelationFilter> correlationFilter(CorrelationFilter filter) {
        FilterRegistrationBean<CorrelationFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(filter);
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE); // Run first to set correlation context
        return reg;
    }

    @Bean
    public FilterRegistrationBean<IdempotencyFilter> idempotencyFilter(HttpIdempotencyRepository repo) {
        FilterRegistrationBean<IdempotencyFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new IdempotencyFilter(repo));
        reg.setOrder(10); // after correlation filter
        return reg;
    }
}
