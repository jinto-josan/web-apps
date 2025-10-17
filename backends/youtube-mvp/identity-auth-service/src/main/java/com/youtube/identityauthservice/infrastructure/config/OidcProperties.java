package com.youtube.identityauthservice.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.oidc")
public class OidcProperties {

    public static class Provider {
        private String issuer;
        private String discoveryUrl; // optional; defaults to issuer + "/.well-known/openid-configuration"
        private String jwksUri;      // optional; resolved from discovery if absent
        private List<String> audiences;

        public String getIssuer() { return issuer; }
        public void setIssuer(String issuer) { this.issuer = issuer; }
        public String getDiscoveryUrl() { return discoveryUrl; }
        public void setDiscoveryUrl(String discoveryUrl) { this.discoveryUrl = discoveryUrl; }
        public String getJwksUri() { return jwksUri; }
        public void setJwksUri(String jwksUri) { this.jwksUri = jwksUri; }
        public List<String> getAudiences() { return audiences; }
        public void setAudiences(List<String> audiences) { this.audiences = audiences; }
    }

    private List<Provider> providers;

    public List<Provider> getProviders() { return providers; }
    public void setProviders(List<Provider> providers) { this.providers = providers; }
}
