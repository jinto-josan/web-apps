package com.youtube.identityauthservice.infrastructure.jwt;

import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.JWTClaimsSet;

public interface JwkProvider {
    JWKSet getPublicJwkSet();
    String getKeyId();
    JWSSigner getSigner();
}
