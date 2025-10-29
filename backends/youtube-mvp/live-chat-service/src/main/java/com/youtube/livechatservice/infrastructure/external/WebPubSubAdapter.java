package com.youtube.livechatservice.infrastructure.external;

import com.azure.messaging.webpubsub.WebPubSubServiceClient;
import com.azure.messaging.webpubsub.WebPubSubServiceClientBuilder;
import com.azure.messaging.webpubsub.models.WebPubSubClientAccessToken;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class WebPubSubAdapter {

    private final WebPubSubServiceClient client;

    public WebPubSubAdapter(@Value("${azure.webpubsub.connection-string}") String connectionString,
                            @Value("${azure.webpubsub.hub:live-chat}") String hub) {
        this.client = new WebPubSubServiceClientBuilder()
                .connectionString(connectionString)
                .hub(hub)
                .buildClient();
    }

    public Token issueClientToken(String userId, Duration ttl) {
        WebPubSubClientAccessToken token = client.getClientAccessToken(options -> options.setUserId(userId));
        Instant exp = Instant.now().plus(ttl);
        return new Token(token.getToken(), exp);
    }

    public record Token(String token, Instant expiresAt) {}
}


