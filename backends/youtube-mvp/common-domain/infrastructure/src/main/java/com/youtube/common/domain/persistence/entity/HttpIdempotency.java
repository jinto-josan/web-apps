package com.youtube.common.domain.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;

/**
 * Base class for HTTP idempotency entities.
 * 
 * <p>This abstract entity implements the HTTP idempotency pattern, allowing clients
 * to safely retry requests by including an Idempotency-Key header. The pattern
 * stores request/response pairs keyed by the idempotency key and request hash,
 * enabling safe retries without duplicate side effects.</p>
 * 
 * <p>Services should extend this class and configure it with their schema-specific
 * table mapping:</p>
 * 
 * <pre>{@code
 * @Entity
 * @Table(name = "http_idempotency", schema = "your_schema",
 *         uniqueConstraints = @UniqueConstraint(
 *             name = "ux_idempotency_key_hash",
 *             columnNames = {"idempotency_key", "request_hash"}
 *         ))
 * public class YourHttpIdempotency extends HttpIdempotency {
 * }
 * }</pre>
 * 
 * <p>The HTTP idempotency pattern ensures:</p>
 * <ul>
 *   <li>Idempotent operations can be safely retried</li>
 *   <li>Duplicate requests return the same response</li>
 *   <li>Request signatures (method + URI + body hash) are validated</li>
 * </ul>
 * 
 * @see <a href="https://datatracker.ietf.org/doc/html/draft-ietf-httpapi-idempotency-key-header">HTTP Idempotency Key Header</a>
 */
@MappedSuperclass
@Getter
@Setter
public abstract class HttpIdempotency {

    /**
     * Surrogate primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Idempotency key provided by the client in the Idempotency-Key header.
     */
    @Column(name = "idempotency_key", length = 128, nullable = false, updatable = false)
    private String idempotencyKey;

    /**
     * SHA-256 hash of the request signature (method + URI + body).
     * Used to detect when the same idempotency key is used with different requests.
     */
    @Column(name = "request_hash", nullable = false, updatable = false, columnDefinition = "bytea")
    private byte[] requestHash;

    /**
     * HTTP status code of the response.
     */
    @Column(name = "response_status")
    private Integer responseStatus;

    /**
     * Response body bytes (cached for retry scenarios).
     */
    @Column(name = "response_body", columnDefinition = "bytea")
    private byte[] responseBody;

    /**
     * Timestamp when the idempotency record was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Timestamp when the idempotency record was last updated.
     */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Sets timestamps before persisting if not already set.
     */
    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    /**
     * Updates the updated timestamp before updating.
     */
    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Checks if this idempotency record has a cached response.
     * 
     * @return true if both responseStatus and responseBody are set, false otherwise
     */
    public boolean hasResponse() {
        return responseStatus != null && responseBody != null;
    }

    /**
     * Stores the response for caching.
     * 
     * @param status the HTTP status code
     * @param body the response body bytes
     */
    public void storeResponse(int status, byte[] body) {
        this.responseStatus = status;
        this.responseBody = body;
        this.updatedAt = Instant.now();
    }

    /**
     * Checks if the given request hash matches this record's request hash.
     * Used to detect idempotency key reuse with different requests.
     * 
     * @param requestHash the request hash to compare
     * @return true if hashes match, false otherwise
     */
    public boolean matchesRequestHash(byte[] requestHash) {
        if (requestHash == null || this.requestHash == null) {
            return false;
        }
        if (requestHash.length != this.requestHash.length) {
            return false;
        }
        for (int i = 0; i < requestHash.length; i++) {
            if (requestHash[i] != this.requestHash[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HttpIdempotency that = (HttpIdempotency) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "HttpIdempotency{" +
                "id=" + id +
                ", idempotencyKey='" + idempotencyKey + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", hasResponse=" + hasResponse() +
                '}';
    }
}

