package com.youtube.userprofileservice.infrastructure.config;

import com.youtube.common.domain.events.EventRouter;
import com.youtube.common.domain.events.UserCreatedEvent;
import com.youtube.userprofileservice.infrastructure.messaging.UserCreatedEventHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for event handlers.
 * Registers event handlers with the EventRouter.
 */
@Configuration
public class EventHandlerConfig {
    
    /**
     * Registers the UserCreatedEventHandler with the EventRouter.
     */
    @Bean
    public EventRouter eventRouter(UserCreatedEventHandler userCreatedEventHandler) {
        EventRouter router = new EventRouter();
        router.registerHandler("user.created", userCreatedEventHandler);
        return router;
    }
}

