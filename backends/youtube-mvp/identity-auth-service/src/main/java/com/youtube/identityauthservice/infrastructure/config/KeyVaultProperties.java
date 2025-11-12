package com.youtube.identityauthservice.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "identity-auth.keyvault")
public class KeyVaultProperties {
    private String vaultUri;
    private String keyName;
    private String keyVersion; // optional; use current if null

    public String getVaultUri() { return vaultUri; }
    public void setVaultUri(String vaultUri) { this.vaultUri = vaultUri; }
    public String getKeyName() { return keyName; }
    public void setKeyName(String keyName) { this.keyName = keyName; }
    public String getKeyVersion() { return keyVersion; }
    public void setKeyVersion(String keyVersion) { this.keyVersion = keyVersion; }
}
