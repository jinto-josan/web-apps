package com.youtube.identityauthservice.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "http_idempotency", schema = "auth",
        uniqueConstraints = {
                @UniqueConstraint(name = "ux_auth_http_idem", columnNames = {"idempotency_key", "request_hash"})
        }
)
@Getter
@Setter
public class HttpIdempotency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", length = 128, nullable = false)
    private String idempotencyKey;

    @Lob
    @Column(name = "request_hash", nullable = false)
    private byte[] requestHash;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Lob
    @Column(name = "response_body")
    private byte[] responseBody;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
