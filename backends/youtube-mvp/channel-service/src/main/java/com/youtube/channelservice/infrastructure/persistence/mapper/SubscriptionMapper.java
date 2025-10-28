package com.youtube.channelservice.infrastructure.persistence.mapper;

import com.youtube.channelservice.domain.models.Subscription;
import com.youtube.channelservice.infrastructure.persistence.entity.SubscriptionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * MapStruct mapper for Subscription domain model.
 */
@Mapper(componentModel = "spring")
public interface SubscriptionMapper {
    
    SubscriptionMapper INSTANCE = Mappers.getMapper(SubscriptionMapper.class);
    
    @Mapping(source = "notificationPreference", target = "notificationPreferenceJson")
    SubscriptionEntity toEntity(Subscription subscription);
    
    @Mapping(source = "notificationPreferenceJson", target = "notificationPreference")
    Subscription toDomain(SubscriptionEntity entity);
}
