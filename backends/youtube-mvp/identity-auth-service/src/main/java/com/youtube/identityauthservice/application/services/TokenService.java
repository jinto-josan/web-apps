package com.youtube.identityauthservice.application.services;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.youtube.identityauthservice.infrastructure.jwt.JwkProvider;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class TokenService {

    private final JwkProvider jwkProvider;
    private final String issuer;
    private final String audience;
    private final int accessTtlSeconds;

    public TokenService(JwkProvider jwkProvider, String issuer, String audience, int accessTtlSeconds) {
        this.jwkProvider = jwkProvider;
        this.issuer = issuer;
        this.audience = audience;
        this.accessTtlSeconds = accessTtlSeconds;
    }

    public String newAccessToken(String userId, String sessionId, String scope, Map<String, Object> extraClaims) {
        Instant now = Instant.now();
        JWTClaimsSet.Builder cb = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject(userId)
                .audience(audience)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(accessTtlSeconds)))
                .jwtID(java.util.UUID.randomUUID().toString())
                .claim("sid", sessionId)
                .claim("scope", scope);

        if (extraClaims != null) {
            extraClaims.forEach(cb::claim);
        }

        SignedJWT jwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256)
                        .type(JOSEObjectType.JWT)
                        .keyID(jwkProvider.getKeyId())
                        .build(),
                cb.build()
        );

        try {
            jwt.sign(jwkProvider.getSigner());
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign access token", e);
        }
        return jwt.serialize();
    }
}
