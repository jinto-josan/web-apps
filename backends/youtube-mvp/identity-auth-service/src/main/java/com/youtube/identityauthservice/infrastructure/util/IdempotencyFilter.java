package com.youtube.identityauthservice.infrastructure.util;

import com.youtube.identityauthservice.domain.model.HttpIdempotency;
import com.youtube.identityauthservice.infrastructure.persistence.HttpIdempotencyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;

public class IdempotencyFilter extends OncePerRequestFilter {

    private final HttpIdempotencyRepository repo;

    public IdempotencyFilter(HttpIdempotencyRepository repo) {
        this.repo = repo;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String method = request.getMethod();
        return !(HttpMethod.POST.matches(method) || HttpMethod.PUT.matches(method) || HttpMethod.PATCH.matches(method));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String key = request.getHeader("Idempotency-Key");
        if (key == null || key.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        byte[] body = StreamUtils.copyToByteArray(request.getInputStream());
        String signature = request.getMethod() + "|" + request.getRequestURI() + "|" + new String(body, StandardCharsets.UTF_8);
        byte[] hash = Hashing.sha256(signature);

        var existing = repo.findByIdempotencyKeyAndRequestHash(key, hash);
        if (existing.isPresent() && existing.get().getResponseStatus() != null && existing.get().getResponseBody() != null) {
            HttpIdempotency idem = existing.get();
            response.setStatus(idem.getResponseStatus());
            response.getOutputStream().write(idem.getResponseBody());
            return;
        }

        ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
        filterChain.doFilter(request, wrapper);

        HttpIdempotency record = existing.orElseGet(HttpIdempotency::new);
        record.setIdempotencyKey(key);
        record.setRequestHash(hash);
        record.setResponseStatus(wrapper.getStatus());
        record.setResponseBody(wrapper.getContentAsByteArray());
        record.setUpdatedAt(Instant.now());
        if (record.getCreatedAt() == null) record.setCreatedAt(Instant.now());
        repo.save(record);

        wrapper.copyBodyToResponse();
    }
}
