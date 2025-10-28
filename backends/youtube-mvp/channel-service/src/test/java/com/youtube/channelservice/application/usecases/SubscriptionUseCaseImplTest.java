package com.youtube.channelservice.application.usecases;

import com.youtube.channelservice.application.commands.SubscribeToChannelCommand;
import com.youtube.channelservice.application.commands.UnsubscribeFromChannelCommand;
import com.youtube.channelservice.domain.models.Subscription;
import com.youtube.channelservice.domain.repositories.ChannelSubscriptionStatsRepository;
import com.youtube.channelservice.domain.repositories.IdempotencyRepository;
import com.youtube.channelservice.domain.repositories.SubscriptionRepository;
import com.youtube.channelservice.domain.services.EventPublisher;
import com.youtube.channelservice.shared.exceptions.ConflictException;
import com.youtube.channelservice.shared.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionUseCaseImplTest {
    
    @Mock
    private SubscriptionRepository subscriptionRepository;
    
    @Mock
    private ChannelSubscriptionStatsRepository statsRepository;
    
    @Mock
    private IdempotencyRepository idempotencyRepository;
    
    @Mock
    private EventPublisher eventPublisher;
    
    private SubscriptionUseCaseImpl subscriptionUseCase;
    
    @BeforeEach
    void setUp() {
        subscriptionUseCase = new SubscriptionUseCaseImpl(
            subscriptionRepository,
            statsRepository,
            idempotencyRepository,
            eventPublisher
        );
    }
    
    @Test
    void subscribeToChannel_Success() {
        // Given
        String userId = "01HZABCDEF";
        String channelId = "01HZGHIJKL";
        String idempotencyKey = "test-idempotency-key";
        
        SubscribeToChannelCommand command = SubscribeToChannelCommand.builder()
            .userId(userId)
            .channelId(channelId)
            .idempotencyKey(idempotencyKey)
            .build();
        
        when(idempotencyRepository.get(idempotencyKey)).thenReturn(Optional.empty());
        when(subscriptionRepository.findByUserIdAndChannelId(userId, channelId))
            .thenReturn(Optional.empty());
        
        ArgumentCaptor<Subscription> subscriptionCaptor = ArgumentCaptor.forClass(Subscription.class);
        when(subscriptionRepository.save(any(Subscription.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        Subscription result = subscriptionUseCase.subscribeToChannel(command);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getChannelId()).isEqualTo(channelId);
        assertThat(result.getIsActive()).isTrue();
        assertThat(result.getShardSuffix()).hasSize(2);
        
        verify(subscriptionRepository).save(any(Subscription.class));
        verify(statsRepository).incrementSubscriberCount(channelId);
        verify(idempotencyRepository).put(idempotencyKey, result.getId(), any());
        verify(eventPublisher).publish(anyString(), any());
    }
    
    @Test
    void subscribeToChannel_Idempotent() {
        // Given
        String userId = "01HZABCDEF";
        String channelId = "01HZGHIJKL";
        String idempotencyKey = "test-idempotency-key";
        String cachedSubscriptionId = "cached-id";
        
        SubscribeToChannelCommand command = SubscribeToChannelCommand.builder()
            .userId(userId)
            .channelId(channelId)
            .idempotencyKey(idempotencyKey)
            .build();
        
        Subscription existingSubscription = Subscription.builder()
            .id(cachedSubscriptionId)
            .userId(userId)
            .channelId(channelId)
            .build();
        
        when(idempotencyRepository.get(idempotencyKey)).thenReturn(Optional.of(cachedSubscriptionId));
        when(subscriptionRepository.findByUserIdAndChannelId(userId, channelId))
            .thenReturn(Optional.of(existingSubscription));
        
        // When
        Subscription result = subscriptionUseCase.subscribeToChannel(command);
        
        // Then
        assertThat(result.getId()).isEqualTo(cachedSubscriptionId);
        verify(subscriptionRepository, never()).save(any());
        verify(statsRepository, never()).incrementSubscriberCount(anyString());
    }
    
    @Test
    void subscribeToChannel_AlreadySubscribed() {
        // Given
        String userId = "01HZABCDEF";
        String channelId = "01HZGHIJKL";
        
        SubscribeToChannelCommand command = SubscribeToChannelCommand.builder()
            .userId(userId)
            .channelId(channelId)
            .idempotencyKey("key")
            .build();
        
        Subscription existing = Subscription.builder()
            .id("existing-id")
            .userId(userId)
            .channelId(channelId)
            .isActive(true)
            .build();
        
        when(idempotencyRepository.get(anyString())).thenReturn(Optional.empty());
        when(subscriptionRepository.findByUserIdAndChannelId(userId, channelId))
            .thenReturn(Optional.of(existing));
        
        // When/Then
        assertThatThrownBy(() -> subscriptionUseCase.subscribeToChannel(command))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("already subscribed");
        
        verify(statsRepository, never()).incrementSubscriberCount(anyString());
    }
    
    @Test
    void unsubscribeFromChannel_Success() {
        // Given
        String userId = "01HZABCDEF";
        String channelId = "01HZGHIJKL";
        String idempotencyKey = "test-idempotency-key";
        
        UnsubscribeFromChannelCommand command = UnsubscribeFromChannelCommand.builder()
            .userId(userId)
            .channelId(channelId)
            .idempotencyKey(idempotencyKey)
            .build();
        
        Subscription subscription = Subscription.builder()
            .id("subscription-id")
            .userId(userId)
            .channelId(channelId)
            .isActive(true)
            .build();
        
        when(idempotencyRepository.get(idempotencyKey)).thenReturn(Optional.empty());
        when(subscriptionRepository.findByUserIdAndChannelId(userId, channelId))
            .thenReturn(Optional.of(subscription));
        
        when(subscriptionRepository.save(any(Subscription.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        subscriptionUseCase.unsubscribeFromChannel(command);
        
        // Then
        verify(subscriptionRepository).save(argThat(sub -> !sub.getIsActive()));
        verify(statsRepository).decrementSubscriberCount(channelId);
        verify(idempotencyRepository).put(idempotencyKey, "unsubscribed", any());
        verify(eventPublisher).publish(anyString(), any());
    }
    
    @Test
    void unsubscribeFromChannel_NotFound() {
        // Given
        String userId = "01HZABCDEF";
        String channelId = "01HZGHIJKL";
        
        UnsubscribeFromChannelCommand command = UnsubscribeFromChannelCommand.builder()
            .userId(userId)
            .channelId(channelId)
            .idempotencyKey("key")
            .build();
        
        when(idempotencyRepository.get(anyString())).thenReturn(Optional.empty());
        when(subscriptionRepository.findByUserIdAndChannelId(userId, channelId))
            .thenReturn(Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> subscriptionUseCase.unsubscribeFromChannel(command))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("not found");
        
        verify(statsRepository, never()).decrementSubscriberCount(anyString());
    }
}
