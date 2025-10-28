package com.youtube.channelservice.application.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * Command to unsubscribe a user from a channel.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnsubscribeFromChannelCommand implements Command {
    
    @NotBlank(message = "User ID cannot be blank")
    private String userId;
    
    @NotBlank(message = "Channel ID cannot be blank")
    private String channelId;
    
    @NotBlank(message = "Idempotency key is required")
    @JsonProperty("idempotencyKey")
    private String idempotencyKey;
    
    private String reason;
}
