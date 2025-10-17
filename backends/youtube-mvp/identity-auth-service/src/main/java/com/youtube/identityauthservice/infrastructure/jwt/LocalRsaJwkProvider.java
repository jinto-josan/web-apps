package com.youtube.identityauthservice.infrastructure.jwt;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.youtube.identityauthservice.infrastructure.config.LocalRsaProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class LocalRsaJwkProvider implements JwkProvider {

    private final String kid;
    private final JWSSigner signer;
    private final RSAKey publicJwk;

    public LocalRsaJwkProvider(LocalRsaProperties props) {
        try {
            this.kid = props.getKid();
            PrivateKey privateKey = loadPrivateKey(props);
            RSAPublicKey publicKey = deriveOrLoadPublicKey(privateKey, props);


            this.signer = new RSASSASigner(privateKey);
            this.publicJwk = new RSAKey.Builder(publicKey)
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(JWSAlgorithm.RS256)
                    .keyID(kid)
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to init LocalRsaJwkProvider", e);
        }
    }

    @Override
    public JWKSet getPublicJwkSet() {
        return new JWKSet(publicJwk);
    }

    @Override
    public String getKeyId() {
        return kid;
    }

    @Override
    public JWSSigner getSigner() {
        return signer;
    }

    private static PrivateKey loadPrivateKey(LocalRsaProperties props) throws Exception {
        String pem = props.getPrivatePem();
        if ((pem == null || pem.isBlank()) && props.getPrivatePemPath() != null) {
            pem = readAll(props.getPrivatePemPath());
        }
        if (pem == null || pem.isBlank()) {
            throw new IllegalArgumentException("localrsa.privatePem or privatePemPath is required");
        }
        byte[] der = pemToDer(pem, "PRIVATE KEY");
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private static RSAPublicKey deriveOrLoadPublicKey(PrivateKey privateKey, LocalRsaProperties props) throws Exception {
        if (privateKey instanceof RSAPrivateCrtKey crt) {
            var spec = new java.security.spec.RSAPublicKeySpec(crt.getModulus(), crt.getPublicExponent());
            return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
        }
        String pem = props.getPublicPem();
        if ((pem == null || pem.isBlank()) && props.getPublicPemPath() != null) {
            pem = readAll(props.getPublicPemPath());
        }
        if (pem == null || pem.isBlank()) {
            throw new IllegalArgumentException("Public key PEM required when private key is not CRT (provide localrsa.publicPem or publicPemPath)");
        }
        byte[] der = pemToDer(pem, "PUBLIC KEY");
        X509EncodedKeySpec spec = new X509EncodedKeySpec(der);
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private static String readAll(String path) throws IOException {
        return Files.readString(Path.of(path));
    }

    private static byte[] pemToDer(String pem, String typeLabel) {
        String norm = pem.replace("-----BEGIN " + typeLabel + "-----", "")
                .replace("-----END " + typeLabel + "-----", "")
                .replaceAll("\s", "");
        return java.util.Base64.getDecoder().decode(norm);
    }
}
