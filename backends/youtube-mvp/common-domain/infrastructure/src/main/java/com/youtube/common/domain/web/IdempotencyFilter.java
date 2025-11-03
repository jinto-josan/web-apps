package com.youtube.common.domain.web;

import com.youtube.common.domain.persistence.idempotency.HttpIdempotencyRepository;
import com.youtube.common.domain.utils.Hashing;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Filter for handling HTTP idempotency using the Idempotency-Key header.
 * 
 * <p>This filter implements the HTTP Idempotency Key Header pattern:
 * - Extracts Idempotency-Key from request header
 * - Creates request signature hash (method + URI + body)
 * - Checks if a cached response exists
 * - Returns cached response if found, otherwise processes request and caches response
 * </p>
 * 
 * <p>Only applies to state-changing HTTP methods: POST, PUT, PATCH.</p>
 * 
 * @see <a href="https://datatracker.ietf.org/doc/html/draft-ietf-httpapi-idempotency-key-header">HTTP Idempotency Key Header</a>
 */
public class IdempotencyFilter extends OncePerRequestFilter {
    
    private static final Logger log = LoggerFactory.getLogger(IdempotencyFilter.class);
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    
    private final HttpIdempotencyRepository repository;
    
    public IdempotencyFilter(HttpIdempotencyRepository repository) {
        this.repository = repository;
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String method = request.getMethod();
        // Only apply to state-changing methods
        return !(HttpMethod.POST.matches(method) || 
                 HttpMethod.PUT.matches(method) || 
                 HttpMethod.PATCH.matches(method));
    }
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, 
            HttpServletResponse response, 
            FilterChain filterChain)
            throws ServletException, IOException {
        
        String idempotencyKey = request.getHeader(IDEMPOTENCY_KEY_HEADER);
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            // No idempotency key, proceed normally
            filterChain.doFilter(request, response);
            return;
        }
        
        // Read request body (need to wrap for reading)
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        byte[] body = wrappedRequest.getContentAsByteArray();
        if (body.length == 0) {
            // Try to read from input stream if not cached
            body = StreamUtils.copyToByteArray(request.getInputStream());
        }
        
        // Create request signature: method + URI + body
        String signature = request.getMethod() + "|" + request.getRequestURI() + "|" 
            + new String(body, StandardCharsets.UTF_8);
        byte[] requestHash = Hashing.sha256(signature);
        
        // Check if we have a cached response
        var storedResponseOpt = repository.findByIdempotencyKeyAndRequestHash(idempotencyKey, requestHash);
        if (storedResponseOpt.isPresent()) {
            HttpIdempotencyRepository.StoredResponse stored = storedResponseOpt.get();
            log.debug("Returning cached response for idempotency key: {}", idempotencyKey);
            response.setStatus(stored.status());
            if (stored.hasBody()) {
                response.getOutputStream().write(stored.body());
            }
            response.getOutputStream().flush();
            return;
        }
        
        // No cached response, process request
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        
        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
            
            // Store response after successful processing
            int status = wrappedResponse.getStatus();
            byte[] responseBody = wrappedResponse.getContentAsByteArray();
            
            repository.storeResponse(idempotencyKey, requestHash, status, responseBody);
            log.debug("Stored idempotency response for key: {}", idempotencyKey);
            
            wrappedResponse.copyBodyToResponse();
        } catch (Exception e) {
            log.error("Error processing request with idempotency key: {}", idempotencyKey, e);
            throw e;
        }
    }
}

