package com.youtube.channelservice.application.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * Query to get subscription statistics for a channel.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetChannelSubscriptionStatsQuery implements Query {
    
    @NotBlank(message = "Channel ID cannot be blank")
    private String channelId;
}
