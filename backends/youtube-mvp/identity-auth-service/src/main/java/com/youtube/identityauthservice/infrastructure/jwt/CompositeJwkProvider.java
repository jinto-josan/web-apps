package com.youtube.identityauthservice.infrastructure.jwt;

import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.jwk.JWKSet;

import java.util.ArrayList;
import java.util.List;

public class CompositeJwkProvider implements JwkProvider {
    private final JwkProvider primary;
    private final List<JwkProvider> publicsOnly;

    public CompositeJwkProvider(JwkProvider primary, List publicsOnly) {
        this.primary = primary;
        this.publicsOnly = publicsOnly != null ? publicsOnly : List.of();
    }

    @Override
    public JWKSet getPublicJwkSet() {
        List<com.nimbusds.jose.jwk.JWK> all = new ArrayList<>(primary.getPublicJwkSet().getKeys());
        for (JwkProvider p : publicsOnly) {
            all.addAll(p.getPublicJwkSet().getKeys());
        }
        return new JWKSet(all);
    }

    @Override
    public String getKeyId() {
        return primary.getKeyId();
    }

    @Override
    public JWSSigner getSigner() {
        return primary.getSigner();
    }
}