package com.youtube.edgecdncontrol.infrastructure.config;

import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.cdn.CdnManager;
import com.azure.resourcemanager.frontdoor.FrontDoorManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AzureConfig {
    
    @Value("${azure.subscription-id}")
    private String subscriptionId;
    
    @Value("${azure.tenant-id:}")
    private String tenantId;
    
    @Bean
    public AzureProfile azureProfile() {
        return new AzureProfile(
                tenantId != null && !tenantId.isEmpty() ? tenantId : null,
                subscriptionId,
                com.azure.core.management.AzureEnvironment.AZURE
        );
    }
    
    @Bean
    public CdnManager cdnManager(AzureProfile azureProfile) {
        return CdnManager.authenticate(
                new DefaultAzureCredentialBuilder().build(),
                azureProfile
        );
    }
    
    @Bean
    public FrontDoorManager frontDoorManager(AzureProfile azureProfile) {
        return FrontDoorManager.authenticate(
                new DefaultAzureCredentialBuilder().build(),
                azureProfile
        );
    }
}

