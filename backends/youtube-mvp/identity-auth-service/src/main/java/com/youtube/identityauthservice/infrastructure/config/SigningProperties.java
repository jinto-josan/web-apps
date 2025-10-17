package com.youtube.identityauthservice.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt.signing")
public class SigningProperties {
    public enum Mode { KEYVAULT, LOCAL }

    private Mode mode = Mode.KEYVAULT;
    private boolean compositePublicJwks = false; // publish both local + keyvault if available
    private boolean fallbackToLocal = true; // if keyvault init fails

    public Mode getMode() { return mode; }
    public void setMode(Mode mode) { this.mode = mode; }
    public boolean isCompositePublicJwks() { return compositePublicJwks; }
    public void setCompositePublicJwks(boolean compositePublicJwks) { this.compositePublicJwks = compositePublicJwks; }
    public boolean isFallbackToLocal() { return fallbackToLocal; }
    public void setFallbackToLocal(boolean fallbackToLocal) { this.fallbackToLocal = fallbackToLocal; }
}
