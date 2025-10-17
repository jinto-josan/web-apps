package com.youtube.identityauthservice.domain.services;

import com.nimbusds.jwt.JWTClaimsSet;
import com.youtube.identityauthservice.infrastructure.jwt.JwkProvider;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class TokenService {

    private final String issuer;
    private final String audience;
    private final int accessTtlSeconds;
    private final JwkProvider jwkProvider;

    public TokenService(String issuer, String audience, int accessTtlSeconds, JwkProvider jwkProvider) {
        this.issuer = issuer;
        this.audience = audience;
        this.accessTtlSeconds = accessTtlSeconds;
        this.jwkProvider = jwkProvider;
    }

    public String newAccessToken(String userId, String sessionId, String scope, Map<String, Object> extraClaims) {
        Instant now = Instant.now();
        JWTClaimsSet.Builder b = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .audience(audience)
                .subject(userId)
                .jwtID(UUID.randomUUID().toString())
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(accessTtlSeconds)))
                .claim("sid", sessionId)
                .claim("scope", scope);
        if (extraClaims != null) extraClaims.forEach(b::claim);
        return jwkProvider.signCompact(b.build());
    }
}
