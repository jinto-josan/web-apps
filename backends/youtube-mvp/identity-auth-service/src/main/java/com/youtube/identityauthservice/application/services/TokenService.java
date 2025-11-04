package com.youtube.identityauthservice.application.services;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.youtube.identityauthservice.infrastructure.jwt.JwkProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

public class TokenService {

    private static final Logger log = LoggerFactory.getLogger(TokenService.class);
    
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
        log.debug("Creating new access token - userId: {}, sessionId: {}, scope: {}", userId, sessionId, scope);
        
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
            log.debug("Access token signed successfully - userId: {}, sessionId: {}, expiresIn: {}s", 
                    userId, sessionId, accessTtlSeconds);
            return jwt.serialize();
        } catch (Exception e) {
            log.error("Failed to sign access token - userId: {}, sessionId: {}", userId, sessionId, e);
            throw new RuntimeException("Failed to sign access token", e);
        }
    }
}
