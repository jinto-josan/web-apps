package com.youtube.identityauthservice.infrastructure.services;

import com.github.f4b6a3.ulid.UlidCreator;
import com.youtube.common.domain.core.IdGenerator;
import com.youtube.common.domain.shared.valueobjects.UserId;
import org.springframework.stereotype.Component;

/**
 * ID generator for User aggregates.
 */
@Component
public class UserIdGenerator implements IdGenerator<UserId> {
    
    @Override
    public UserId nextId() {
        return UserId.from(UlidCreator.getUlid().toString());
    }
}

