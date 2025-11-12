package com.youtube.identityauthservice.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "identity-auth.localrsa")
@Getter
@Setter
public class LocalRsaProperties {
    private String kid = "local-key";
    // Provide either inline PEM content or a path; PKCS#8 private key is required
    private String privatePem;
    private String privatePemPath;
    // Optional public key PEM (if private key doesn’t include CRT info)
    private String publicPem;
    private String publicPemPath;

}