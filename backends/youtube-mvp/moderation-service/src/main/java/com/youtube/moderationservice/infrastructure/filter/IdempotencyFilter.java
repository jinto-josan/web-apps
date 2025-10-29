package com.youtube.moderationservice.infrastructure.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
public class IdempotencyFilter extends OncePerRequestFilter {
    private static final String IDEMPOTENCY_KEY = "Idempotency-Key";
    private final StringRedisTemplate redisTemplate;

    public IdempotencyFilter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String key = request.getHeader(IDEMPOTENCY_KEY);
        if (key != null && !key.isBlank()) {
            Boolean success = redisTemplate.opsForValue().setIfAbsent("idem:" + key, "1", Duration.ofMinutes(10));
            if (Boolean.FALSE.equals(success)) {
                response.setStatus(409);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}


