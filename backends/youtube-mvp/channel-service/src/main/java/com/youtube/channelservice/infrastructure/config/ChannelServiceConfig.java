package com.youtube.channelservice.infrastructure.config;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.youtube.channelservice.application.commands.ChannelCommandHandler;
import com.youtube.channelservice.application.commands.ChannelCommandHandlerImpl;
import com.youtube.channelservice.domain.repositories.ChannelRepository;
import com.youtube.channelservice.domain.repositories.ChannelMemberRepository;
import com.youtube.channelservice.domain.repositories.HandleRegistry;
import com.youtube.channelservice.domain.services.BlobUriValidator;
import com.youtube.channelservice.domain.services.ReservedWords;
import com.youtube.channelservice.infrastructure.persistence.CosmosChannelRepository;
import com.youtube.channelservice.infrastructure.persistence.CosmosChannelMemberRegistry;
import com.youtube.channelservice.infrastructure.persistence.CosmosHandleRegistry;
import com.youtube.channelservice.interfaces.events.EventPublisher;
import com.youtube.channelservice.interfaces.persistence.CacheAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Set;

/**
 * Configuration class for wiring up the channel service components.
 * Implements dependency injection for Command Pattern and Saga Pattern components.
 */
@Configuration
public class ChannelServiceConfig {
    
    @Value("${cosmos.database.name}")
    private String databaseName;
    
    @Value("${cosmos.container.channels}")
    private String channelsContainerName;
    
    @Value("${cosmos.container.handles}")
    private String handlesContainerName;
    
    @Value("${cosmos.container.members}")
    private String membersContainerName;
    
    @Value("${blob.allowed-origins}")
    private Set<String> allowedBlobOrigins;
    
    @Value("${handles.reserved-words}")
    private Set<String> reservedHandles;
    
    @Bean
    public CosmosContainer channelsContainer(CosmosClient cosmosClient) {
        return cosmosClient.getDatabase(databaseName).getContainer(channelsContainerName);
    }
    
    @Bean
    public CosmosContainer handlesContainer(CosmosClient cosmosClient) {
        return cosmosClient.getDatabase(databaseName).getContainer(handlesContainerName);
    }
    
    @Bean
    public CosmosContainer membersContainer(CosmosClient cosmosClient) {
        return cosmosClient.getDatabase(databaseName).getContainer(membersContainerName);
    }
    
    @Bean
    public ChannelRepository channelRepository(CosmosContainer channelsContainer) {
        return new CosmosChannelRepository(channelsContainer);
    }
    
    @Bean
    public HandleRegistry handleRegistry(CosmosContainer handlesContainer) {
        return new CosmosHandleRegistry(handlesContainer);
    }
    
    @Bean
    public ChannelMemberRepository channelMemberRepository(CosmosContainer membersContainer) {
        return new CosmosChannelMemberRegistry(membersContainer);
    }
    
    @Bean
    public CacheAdapter cacheAdapter(StringRedisTemplate redisTemplate) {
        return new RedisCacheAdapter(redisTemplate);
    }
    
    @Bean
    public BlobUriValidator blobUriValidator() {
        return new BlobUriValidator(allowedBlobOrigins);
    }
    
    @Bean
    public ReservedWords reservedWords() {
        return new ReservedWords(reservedHandles);
    }
    
    @Bean
    public ChannelCommandHandler channelCommandHandler(
            ChannelRepository channelRepository,
            HandleRegistry handleRegistry,
            ChannelMemberRepository channelMemberRepository,
            EventPublisher eventPublisher,
            CacheAdapter cacheAdapter,
            BlobUriValidator blobUriValidator,
            ReservedWords reservedWords) {
        
        return new ChannelCommandHandlerImpl(
            channelRepository,
            handleRegistry,
            channelMemberRepository,
            eventPublisher,
            cacheAdapter,
            blobUriValidator,
            reservedWords
        );
    }
}
