package com.youtube.mvp.search.infrastructure.configuration;

import com.youtube.mvp.search.domain.service.SearchService;
import com.youtube.mvp.search.infrastructure.adapter.AzureSearchAdapter;
import com.youtube.mvp.search.infrastructure.client.AzureSearchClient;
import com.youtube.mvp.search.infrastructure.repository.CosmosVideoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SearchConfiguration {
    
    @Bean
    public SearchService searchService(AzureSearchClient azureSearchClient, 
                                        CosmosVideoRepository cosmosVideoRepository) {
        return new AzureSearchAdapter(azureSearchClient, cosmosVideoRepository);
    }
}
