package com.youtube.identityauthservice.infrastructure.jwt;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyClientBuilder;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.JWSSigner;
import com.youtube.identityauthservice.infrastructure.config.KeyVaultProperties;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;

public class KeyVaultJwkProvider implements JwkProvider {

    private final KeyClient keyClient;
    private final CryptographyClient cryptoClient;
    private final String kid;
    private final RSAKey publicRsaKey;
    private final JWSSigner signer;

    public KeyVaultJwkProvider(KeyVaultProperties props) {
        var credential = new DefaultAzureCredentialBuilder().build();


        KeyClient kc = new KeyClientBuilder()
                .vaultUrl(props.getVaultUri())
                .credential(credential)
                .buildClient();

        KeyVaultKey kvKey = (props.getKeyVersion() == null)
                ? kc.getKey(props.getKeyName())
                : kc.getKey(props.getKeyName(), props.getKeyVersion());

        String keyId = kvKey.getId();
        this.kid = keyId;

        this.cryptoClient = new CryptographyClientBuilder()
                .keyIdentifier(keyId)
                .credential(credential)
                .buildClient();

        JsonWebKey jwk = kvKey.getKey();
        byte[] n = jwk.getN();
        byte[] e = jwk.getE();
        if (n == null || e == null) {
            throw new IllegalStateException("Key Vault RSA key missing modulus/exponent");
        }
        var pubSpec = new RSAPublicKeySpec(new BigInteger(1, n), new BigInteger(1, e));
        RSAPublicKey publicKey;
        try {
            publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(pubSpec);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to build RSAPublicKey from Key Vault JWK", ex);
        }

        this.publicRsaKey = new RSAKey.Builder(publicKey)
                .keyID(kid)
                .algorithm(JWSAlgorithm.RS256)
                .keyUse(KeyUse.SIGNATURE)
                .build();

        this.signer = new KeyVaultRs256Signer(cryptoClient);
        this.keyClient = kc;
    }

    @Override
    public JWKSet getPublicJwkSet() {
        return new JWKSet(publicRsaKey);
    }

    @Override
    public String getKeyId() {
        return kid;
    }

    @Override
    public JWSSigner getSigner() {
        return signer;
    }
}
