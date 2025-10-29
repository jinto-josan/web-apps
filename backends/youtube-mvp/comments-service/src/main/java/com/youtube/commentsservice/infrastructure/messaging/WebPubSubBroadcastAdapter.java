package com.youtube.commentsservice.infrastructure.messaging;

import com.azure.messaging.webpubsub.WebPubSubServiceClient;
import com.youtube.commentsservice.domain.services.BroadcastPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Adapter for broadcasting real-time updates via Azure Web PubSub
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebPubSubBroadcastAdapter implements BroadcastPort {
    
    private final WebPubSubServiceClient pubSubClient;
    
    @Override
    public void broadcastCommentCreated(String videoId, String commentJson) {
        try {
            String hub = "videos";
            String groupId = "video-" + videoId;
            
            pubSubClient.sendToGroup(groupId, commentJson, "application/json");
            log.debug("Broadcasted comment created to group: {}", groupId);
        } catch (Exception e) {
            log.error("Failed to broadcast comment created event", e);
            // Don't throw - broadcasting is best effort
        }
    }
    
    @Override
    public void broadcastCommentDeleted(String videoId, String commentId) {
        try {
            String hub = "videos";
            String groupId = "video-" + videoId;
            String message = String.format("{\"event\":\"commentDeleted\",\"commentId\":\"%s\"}", commentId);
            
            pubSubClient.sendToGroup(groupId, message, "application/json");
            log.debug("Broadcasted comment deleted to group: {}", groupId);
        } catch (Exception e) {
            log.error("Failed to broadcast comment deleted event", e);
            // Don't throw - broadcasting is best effort
        }
    }
}

