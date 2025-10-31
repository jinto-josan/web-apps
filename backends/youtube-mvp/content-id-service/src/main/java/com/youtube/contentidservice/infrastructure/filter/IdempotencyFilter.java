package com.youtube.contentidservice.infrastructure.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class IdempotencyFilter extends OncePerRequestFilter {
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    private static final String IDEMPOTENCY_KEY_PREFIX = "idempotency:";
    private static final Duration IDEMPOTENCY_TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Only apply to state-changing methods
        if (!isStateChangingMethod(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String idempotencyKey = request.getHeader(IDEMPOTENCY_KEY_HEADER);
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        String redisKey = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
        
        // Try to set the key atomically
        Boolean setIfAbsent = redisTemplate.opsForValue().setIfAbsent(redisKey, "processing", IDEMPOTENCY_TTL);
        
        if (Boolean.FALSE.equals(setIfAbsent)) {
            // Key already exists - this is a duplicate request
            log.warn("Duplicate request detected with idempotency key: {}", idempotencyKey);
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Duplicate request detected\",\"idempotencyKey\":\"" + idempotencyKey + "\"}");
            return;
        }

        try {
            filterChain.doFilter(request, response);
            
            // Store response if successful
            if (response.getStatus() >= 200 && response.getStatus() < 300) {
                redisTemplate.opsForValue().set(redisKey, String.valueOf(response.getStatus()), IDEMPOTENCY_TTL);
            }
        } catch (Exception e) {
            // Remove key on error to allow retry
            redisTemplate.delete(redisKey);
            throw e;
        }
    }

    private boolean isStateChangingMethod(String method) {
        return HttpMethod.POST.matches(method) || 
               HttpMethod.PUT.matches(method) || 
               HttpMethod.PATCH.matches(method) || 
               HttpMethod.DELETE.matches(method);
    }
}

