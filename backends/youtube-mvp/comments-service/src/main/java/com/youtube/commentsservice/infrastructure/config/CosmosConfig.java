package com.youtube.commentsservice.infrastructure.config;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.spring.data.cosmos.config.AbstractCosmosConfiguration;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.repository.config.EnableCosmosRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableCosmosRepositories(basePackages = "com.youtube.commentsservice.infrastructure.persistence")
public class CosmosConfig extends AbstractCosmosConfiguration {
    
    @Value("${azure.cosmos.database}")
    private String databaseName;
    
    @Value("${azure.cosmos.endpoint}")
    private String uri;
    
    @Value("${azure.cosmos.key}")
    private String key;
    
    @Bean
    public CosmosClient cosmosClient() {
        return new CosmosClientBuilder()
                .endpoint(uri)
                .key(key)
                .buildClient();
    }
    
    @Override
    protected String getDatabaseName() {
        return databaseName;
    }
    
    @Override
    protected CosmosConfig getCosmosConfig() {
        return CosmosConfig.builder()
                .enableQueryMetrics(true)
                .build();
    }
}

