package com.youtube.channelservice.application.usecases;

import com.youtube.channelservice.application.commands.SubscribeToChannelCommand;
import com.youtube.channelservice.application.commands.UnsubscribeFromChannelCommand;
import com.youtube.channelservice.application.queries.GetChannelSubscriptionStatsQuery;
import com.youtube.channelservice.application.queries.GetUserSubscriptionsQuery;
import com.youtube.channelservice.domain.models.ChannelSubscriptionStats;
import com.youtube.channelservice.domain.models.Subscription;
import com.youtube.channelservice.domain.models.UserSubscriptionInfo;

/**
 * Use case interface for subscription operations.
 */
public interface SubscriptionUseCase {
    
    /**
     * Subscribe a user to a channel.
     */
    Subscription subscribeToChannel(SubscribeToChannelCommand command);
    
    /**
     * Unsubscribe a user from a channel.
     */
    void unsubscribeFromChannel(UnsubscribeFromChannelCommand command);
    
    /**
     * Get all subscriptions for a user.
     */
    UserSubscriptionInfo getUserSubscriptions(GetUserSubscriptionsQuery query);
    
    /**
     * Get subscription statistics for a channel.
     */
    ChannelSubscriptionStats getChannelSubscriptionStats(GetChannelSubscriptionStatsQuery query);
    
    /**
     * Check if a user is subscribed to a channel.
     */
    boolean isSubscribed(String userId, String channelId);
}
