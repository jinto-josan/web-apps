package com.youtube.identityauthservice.infrastructure.config;

import com.youtube.identityauthservice.infrastructure.jwt.CompositeJwkProvider;
import com.youtube.identityauthservice.infrastructure.jwt.JwkProvider;
import com.youtube.identityauthservice.infrastructure.jwt.KeyVaultJwkProvider;
import com.youtube.identityauthservice.infrastructure.jwt.LocalRsaJwkProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Configuration
@EnableConfigurationProperties({SigningProperties.class, LocalRsaProperties.class, KeyVaultProperties.class})
public class SigningConfig {

    @Bean
    public JwkProvider jwkProvider(SigningProperties signing,
                                   LocalRsaProperties localProps,
                                   KeyVaultProperties kvProps) {
        JwkProvider local = null;
        try {
            local = new LocalRsaJwkProvider(localProps);
        } catch (Exception e) {
// only log; we may not need local
        }


        if (signing.getMode() == SigningProperties.Mode.KEYVAULT) {
            try {
                JwkProvider kv = new KeyVaultJwkProvider(kvProps);
                if (signing.isCompositePublicJwks() && local != null) {
                    return new CompositeJwkProvider(kv, java.util.List.of(local));
                }
                return kv;
            } catch (RuntimeException kvEx) {
                if (signing.isFallbackToLocal() && local != null) {
                    return local;
                }
                throw kvEx;
            }
        } else {
            if (local == null) throw new IllegalStateException("Local RSA provider initialization failed");
            return local;
        }
    }
}
