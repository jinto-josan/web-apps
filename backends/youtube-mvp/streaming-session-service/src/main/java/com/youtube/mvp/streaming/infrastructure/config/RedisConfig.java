package com.youtube.mvp.streaming.infrastructure.config;

import com.youtube.mvp.streaming.domain.model.PlaybackSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis configuration for session storage.
 */
@Configuration
@Slf4j
public class RedisConfig {
    
    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;
    
    @Value("${spring.data.redis.port:6379}")
    private int redisPort;
    
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        log.info("Configuring Redis connection: {}:{}", redisHost, redisPort);
        
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        
        return new LettuceConnectionFactory(config);
    }
    
    @Bean
    public RedisTemplate<String, PlaybackSession> playbackSessionRedisTemplate(
            LettuceConnectionFactory connectionFactory) {
        
        RedisTemplate<String, PlaybackSession> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Key serializer
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Value serializer
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }
}

