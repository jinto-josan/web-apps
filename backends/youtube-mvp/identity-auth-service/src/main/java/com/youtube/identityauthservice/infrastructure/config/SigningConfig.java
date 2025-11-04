package com.youtube.identityauthservice.infrastructure.config;

import com.youtube.identityauthservice.infrastructure.jwt.CompositeJwkProvider;
import com.youtube.identityauthservice.infrastructure.jwt.JwkProvider;
import com.youtube.identityauthservice.infrastructure.jwt.KeyVaultJwkProvider;
import com.youtube.identityauthservice.infrastructure.jwt.LocalRsaJwkProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Configuration
@EnableConfigurationProperties({SigningProperties.class, LocalRsaProperties.class, KeyVaultProperties.class})
public class SigningConfig {

    private static final Logger log = LoggerFactory.getLogger(SigningConfig.class);

    @Bean
    public JwkProvider jwkProvider(SigningProperties signing,
                                   LocalRsaProperties localProps,
                                   KeyVaultProperties kvProps) {
        log.info("Initializing JwkProvider - mode: {}, compositePublicJwks: {}, fallbackToLocal: {}", 
                signing.getMode(), signing.isCompositePublicJwks(), signing.isFallbackToLocal());
        
        JwkProvider local = null;
        try {
            log.debug("Attempting to initialize LocalRsaJwkProvider");
            local = new LocalRsaJwkProvider(localProps);
            log.info("LocalRsaJwkProvider initialized successfully");
        } catch (Exception e) {
            log.warn("Failed to initialize LocalRsaJwkProvider: {}", e.getMessage());
            // only log; we may not need local
        }


        if (signing.getMode() == SigningProperties.Mode.KEYVAULT) {
            log.debug("Using KeyVault mode for JwkProvider");
            try {
                JwkProvider kv = new KeyVaultJwkProvider(kvProps);
                log.info("KeyVaultJwkProvider initialized successfully");
                
                if (signing.isCompositePublicJwks() && local != null) {
                    log.info("Creating CompositeJwkProvider with KeyVault and Local providers");
                    return new CompositeJwkProvider(kv, java.util.List.of(local));
                }
                return kv;
            } catch (RuntimeException kvEx) {
                log.error("Failed to initialize KeyVaultJwkProvider: {}", kvEx.getMessage(), kvEx);
                if (signing.isFallbackToLocal() && local != null) {
                    log.warn("Falling back to LocalRsaJwkProvider due to KeyVault initialization failure");
                    return local;
                }
                throw kvEx;
            }
        } else {
            log.debug("Using Local mode for JwkProvider");
            if (local == null) {
                log.error("Local RSA provider initialization failed and mode is LOCAL");
                throw new IllegalStateException("Local RSA provider initialization failed");
            }
            return local;
        }
    }
}
