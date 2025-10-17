package com.youtube.identityauthservice.interfaces.rest;

import com.nimbusds.jose.jwk.JWKSet;
import com.youtube.identityauthservice.infrastructure.jwt.JwkProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class JwksController {

    private final JwkProvider provider;

    public JwksController(JwkProvider provider) {
        this.provider = provider;
    }

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwks() {
        JWKSet set = provider.getPublicJwkSet();
        return set.toJSONObject(true);
    }
}