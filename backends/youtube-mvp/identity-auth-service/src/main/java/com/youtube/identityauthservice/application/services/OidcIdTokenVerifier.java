package com.youtube.identityauthservice.domain.services;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJOSEObjectTypeVerifier;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.youtube.identityauthservice.infrastructure.config.OidcProperties;

import java.net.URL;
import java.text.ParseException;
import java.time.Instant;
import java.util.*;

public class OidcIdTokenVerifier {

    private static class ProviderVerifier {
        final String issuer;
        final Set<String> audiences;
        final ConfigurableJWTProcessor<SecurityContext> proc;

        ProviderVerifier(String issuer, List<String> audiences, String jwksUri) {
            try {
                this.issuer = issuer;
                this.audiences = new HashSet<>(audiences);
                JWKSource<SecurityContext> jwkSource =
                        new RemoteJWKSet<>(new URL(jwksUri), new DefaultResourceRetriever(2000, 2000));
                DefaultJWTProcessor<SecurityContext> p = new DefaultJWTProcessor<>();
                JWSKeySelector<SecurityContext> keySelector =
                        new com.nimbusds.jose.proc.JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwkSource);
                p.setJWSKeySelector(keySelector);
                p.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>());
                this.proc = p;
            } catch (Exception e) {
                throw new RuntimeException("Failed to init OIDC verifier", e);
            }
        }

        JWTClaimsSet process(SignedJWT jwt) throws Exception {
            return proc.process(jwt, null);
        }
    }

    private final Map<String, ProviderVerifier> byIssuer;

    public OidcIdTokenVerifier(OidcProperties props) {
        Map<String, ProviderVerifier> m = new HashMap<>();
        if (props.getProviders() != null) {
            for (OidcProperties.Provider p : props.getProviders()) {
                m.put(p.getIssuer(), new ProviderVerifier(p.getIssuer(), p.getAudiences(), p.getJwksUri()));
            }
        }
        this.byIssuer = Collections.unmodifiableMap(m);
    }

    public JWTClaimsSet verify(String idToken) {
        try {
            SignedJWT jwt = SignedJWT.parse(idToken);
            String iss = jwt.getJWTClaimsSet().getIssuer();
            ProviderVerifier v = byIssuer.get(iss);
            if (v == null) throw new SecurityException("Issuer not allowed");
            JWTClaimsSet claims = v.process(jwt);

            Date exp = claims.getExpirationTime();
            if (exp == null || Instant.now().isAfter(exp.toInstant())) throw new SecurityException("id_token expired");

            List<String> aud = claims.getAudience();
            if (aud == null || aud.stream().noneMatch(v.audiences::contains))
                throw new SecurityException("aud not allowed");

            return claims;
        } catch (ParseException e) {
            throw new SecurityException("Invalid id_token", e);
        } catch (Exception e) {
            throw new SecurityException("id_token verification failed", e);
        }
    }
}