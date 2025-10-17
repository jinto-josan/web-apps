package com.youtube.identityauthservice.application.services;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.JSONObjectUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.JOSEException;
import com.youtube.identityauthservice.infrastructure.config.OidcProperties;

import java.net.URL;
import java.text.ParseException;
import java.time.Instant;
import java.util.*;

public class OidcIdTokenVerifier {

    public record VerifiedIdentity(
            String subject,
            String issuer,
            List<String> audiences,
            String email,
            String name,
            boolean emailVerified,
            JWTClaimsSet rawClaims
    ) {}

    private static class ProviderVerifier {
        final String issuer;
        final Set<String> audiences;
        final ConfigurableJWTProcessor<SecurityContext> proc;

        ProviderVerifier(String issuer, List<String> audiences, String jwksUri) {
            try {
                this.issuer = issuer;
                this.audiences = new HashSet<>(audiences);
                DefaultResourceRetriever retriever = new DefaultResourceRetriever(3000, 3000);
                JWKSource<SecurityContext> jwkSource = new RemoteJWKSet<>(new URL(jwksUri), retriever);
                DefaultJWTProcessor<SecurityContext> p = new DefaultJWTProcessor<>();
                JWSKeySelector<SecurityContext> keySelector =
                        new com.nimbusds.jose.proc.JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwkSource);
                p.setJWSKeySelector(keySelector);
                this.proc = p;
            } catch (Exception e) {
                throw new RuntimeException("Failed to init OIDC verifier", e);
            }
        }

        JWTClaimsSet process(SignedJWT jwt) throws BadJOSEException, JOSEException {
            return proc.process(jwt, null);
        }
    }

    private final Map<String, ProviderVerifier> byIssuer;

    public OidcIdTokenVerifier(OidcProperties props) {
        Map<String, ProviderVerifier> m = new HashMap<>();
        if (props.getProviders() != null) {
            for (OidcProperties.Provider p : props.getProviders()) {
                String iss = Objects.requireNonNull(p.getIssuer(), "issuer required");
                String jwksUri = (p.getJwksUri() != null && !p.getJwksUri().isBlank())
                        ? p.getJwksUri()
                        : discoverJwksUri(p.getDiscoveryUrl() != null ? p.getDiscoveryUrl() : defaultDiscovery(iss));
                m.put(iss, new ProviderVerifier(iss, p.getAudiences(), jwksUri));
            }
        }
        this.byIssuer = Collections.unmodifiableMap(m);
    }

    private String defaultDiscovery(String issuer) {
        String base = issuer.endsWith("/") ? issuer.substring(0, issuer.length() - 1) : issuer;
        return base + "/.well-known/openid-configuration";
    }

    private String discoverJwksUri(String discoveryUrl) {
        try {
            DefaultResourceRetriever retriever = new DefaultResourceRetriever(3000, 3000);
            var res = retriever.retrieveResource(new URL(discoveryUrl));
            var json = JSONObjectUtils.parse(res.getContent());
            String jwks = (String) json.get("jwks_uri");
            if (jwks == null || jwks.isBlank()) throw new IllegalStateException("jwks_uri missing in discovery: " + discoveryUrl);
            return jwks;
        } catch (Exception e) {
            throw new RuntimeException("OIDC discovery failed: " + discoveryUrl, e);
        }
    }

    public VerifiedIdentity verify(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            String iss = jwt.getJWTClaimsSet().getIssuer();
            ProviderVerifier v = byIssuer.get(iss);
            if (v == null) throw new SecurityException("Issuer not allowed");

            JWTClaimsSet claims = v.process(jwt);

            Date exp = claims.getExpirationTime();
            if (exp == null || Instant.now().isAfter(exp.toInstant())) throw new SecurityException("token expired");

            List<String> aud = claims.getAudience();
            if (aud == null || aud.stream().noneMatch(v.audiences::contains)) throw new SecurityException("aud not allowed");

            String email = resolveEmail(claims);
            String name = resolveName(claims);
            boolean emailVerified = Optional.ofNullable(asBoolean(claims, "email_verified")).orElse(true);

            return new VerifiedIdentity(claims.getSubject(), iss, aud, email, name, emailVerified, claims);
        } catch (ParseException e) {
            throw new SecurityException("Invalid token", e);
        } catch (BadJOSEException | JOSEException e) {
            throw new SecurityException("token verification failed", e);
        }
    }

    private static String resolveEmail(JWTClaimsSet claims) {
        String email = asString(claims, "email");
        if (email != null && !email.isBlank()) return email;
        email = asString(claims, "preferred_username");
        if (email != null && !email.isBlank()) return email;
        email = asString(claims, "upn");
        if (email != null && !email.isBlank()) return email;
        Object emails = claims.getClaim("emails");
        if (emails instanceof java.util.List<?> list && !list.isEmpty()) {
            Object first = list.get(0);
            if (first instanceof String s && !s.isBlank()) return s;
        }
        return null;
    }

    private static String resolveName(JWTClaimsSet claims) {
        String name = asString(claims, "name");
        if (name != null && !name.isBlank()) return name;
        String given = asString(claims, "given_name");
        String family = asString(claims, "family_name");
        if (given != null || family != null) {
            return ((given != null) ? given : "") + ((given != null && family != null) ? " " : "") + ((family != null) ? family : "");
        }
        return Optional.ofNullable(resolveEmail(claims)).orElse(claims.getSubject());
    }

    private static String asString(JWTClaimsSet claims, String key) {
        Object v = claims.getClaim(key);
        return (v instanceof String s) ? s : null;
    }

    private static Boolean asBoolean(JWTClaimsSet claims, String key) {
        Object v = claims.getClaim(key);
        return (v instanceof Boolean b) ? b : null;
    }
}

