package com.youtube.mvp.streaming.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * Observability configuration for correlation IDs and tracing.
 */
@Configuration
@Slf4j
public class ObservabilityConfig {
    
    @Bean
    public OncePerRequestFilter correlationIdFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    FilterChain filterChain) throws ServletException, IOException {
                
                String correlationId = request.getHeader("X-Correlation-ID");
                if (correlationId == null || correlationId.isEmpty()) {
                    correlationId = UUID.randomUUID().toString();
                }
                
                MDC.put("correlationId", correlationId);
                MDC.put("requestPath", request.getRequestURI());
                MDC.put("requestMethod", request.getMethod());
                
                response.setHeader("X-Correlation-ID", correlationId);
                
                try {
                    filterChain.doFilter(request, response);
                } finally {
                    MDC.clear();
                }
            }
        };
    }
}

