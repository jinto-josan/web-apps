package com.youtube.identityauthservice.infrastructure.jwt;

import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSSigner;

import java.security.MessageDigest;
import java.util.Collections;
import java.util.Set;

public class KeyVaultRs256Signer implements JWSSigner {

    private final CryptographyClient cryptoClient;
    private final com.nimbusds.jose.jca.JCAContext jcaContext = new com.nimbusds.jose.jca.JCAContext();



    public KeyVaultRs256Signer(CryptographyClient cryptoClient) {
        this.cryptoClient = cryptoClient;
    }

    @Override
    public Set<JWSAlgorithm> supportedJWSAlgorithms() {
        return Collections.singleton(JWSAlgorithm.RS256);
    }

    @Override
    public com.nimbusds.jose.util.Base64URL sign(com.nimbusds.jose.JWSHeader header, byte[] signingInput)
            throws com.nimbusds.jose.JOSEException {
        if (!com.nimbusds.jose.JWSAlgorithm.RS256.equals(header.getAlgorithm())) {
            throw new com.nimbusds.jose.JOSEException("Unsupported alg: " + header.getAlgorithm());
        }
        try {
            var result = cryptoClient.signData(com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm.RS256, signingInput);
            return com.nimbusds.jose.util.Base64URL.encode(result.getSignature());
        } catch (RuntimeException ex) {
            throw new com.nimbusds.jose.JOSEException("Key Vault sign failed", ex);
        }
    }
    @Override
    public com.nimbusds.jose.jca.JCAContext getJCAContext() {
        return jcaContext;
    }

}