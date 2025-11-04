package com.youtube.identityauthservice.interfaces.rest;

import com.nimbusds.jose.jwk.JWKSet;
import com.youtube.identityauthservice.infrastructure.jwt.JwkProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class JwksController {

    private static final Logger log = LoggerFactory.getLogger(JwksController.class);
    private final JwkProvider provider;

    public JwksController(JwkProvider provider) {
        this.provider = provider;
    }

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwks() {
        log.debug("JWKS endpoint requested");
        try {
            JWKSet set = provider.getPublicJwkSet();
            log.debug("JWKS returned successfully - keyCount: {}", set.getKeys().size());
            return set.toJSONObject(true);
        } catch (Exception e) {
            log.error("Failed to retrieve JWKS", e);
            throw e;
        }
    }
}