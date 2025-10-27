package com.youtube.channelservice.infrastructure.config;

import com.youtube.channelservice.application.commands.ChannelCommandHandler;
import com.youtube.channelservice.application.commands.ChannelCommandHandlerImpl;
import com.youtube.channelservice.domain.repositories.ChannelRepository;
import com.youtube.channelservice.domain.repositories.ChannelMemberRepository;
import com.youtube.channelservice.domain.repositories.HandleRegistry;
import com.youtube.channelservice.domain.services.EventPublisher;
import com.youtube.channelservice.domain.services.CacheService;
import com.youtube.channelservice.domain.services.BlobUriValidator;
import com.youtube.channelservice.domain.services.ReservedWordsService;
import com.youtube.channelservice.infrastructure.persistence.repository.ChannelJpaRepository;
import com.youtube.channelservice.infrastructure.persistence.repository.ChannelMemberJpaRepository;
import com.youtube.channelservice.infrastructure.persistence.repository.HandleJpaRepository;
import com.youtube.channelservice.infrastructure.persistence.service.ChannelRepositoryImpl;
import com.youtube.channelservice.infrastructure.persistence.service.ChannelMemberRepositoryImpl;
import com.youtube.channelservice.infrastructure.persistence.service.HandleRegistryImpl;
import com.youtube.channelservice.infrastructure.services.EventPublisherImpl;
import com.youtube.channelservice.infrastructure.services.CacheServiceImpl;
import com.youtube.channelservice.infrastructure.services.BlobUriValidatorImpl;
import com.youtube.channelservice.infrastructure.services.ReservedWordsServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Set;

/**
 * Configuration class for wiring up the channel service components with Spring Data JPA.
 * Implements dependency injection for Command Pattern and Saga Pattern components.
 * Follows Clean Architecture principles with proper dependency inversion.
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.youtube.channelservice.infrastructure.persistence.repository")
@EnableAspectJAutoProxy
public class ChannelServiceJpaConfig {
    
    @Value("${blob.allowed-origins}")
    private Set<String> allowedBlobOrigins;
    
    @Value("${handles.reserved-words}")
    private Set<String> reservedHandles;
    
    // Domain Repository Implementations
    @Bean
    public ChannelRepository channelRepository(ChannelJpaRepository jpaRepository) {
        return new ChannelRepositoryImpl(jpaRepository);
    }
    
    @Bean
    public HandleRegistry handleRegistry(HandleJpaRepository jpaRepository) {
        return new HandleRegistryImpl(jpaRepository);
    }
    
    @Bean
    public ChannelMemberRepository channelMemberRepository(ChannelMemberJpaRepository jpaRepository) {
        return new ChannelMemberRepositoryImpl(jpaRepository);
    }
    
    // Domain Service Implementations
    @Bean
    public EventPublisher eventPublisher() {
        return new EventPublisherImpl();
    }
    
    @Bean
    public CacheService cacheService(StringRedisTemplate redisTemplate) {
        return new CacheServiceImpl(redisTemplate);
    }
    
    @Bean
    public BlobUriValidator blobUriValidator() {
        return new BlobUriValidatorImpl(allowedBlobOrigins);
    }
    
    @Bean
    public ReservedWordsService reservedWordsService() {
        return new ReservedWordsServiceImpl(reservedHandles);
    }
    
    // Application Layer
    @Bean
    public ChannelCommandHandler channelCommandHandler(
            ChannelRepository channelRepository,
            HandleRegistry handleRegistry,
            ChannelMemberRepository channelMemberRepository,
            EventPublisher eventPublisher,
            CacheService cacheService,
            BlobUriValidator blobUriValidator,
            ReservedWordsService reservedWordsService) {
        
        return new ChannelCommandHandlerImpl(
            channelRepository,
            handleRegistry,
            channelMemberRepository,
            eventPublisher,
            cacheService,
            blobUriValidator,
            reservedWordsService
        );
    }
}