package com.youtube.channelservice.infrastructure.persistence.mapper;

import com.youtube.channelservice.domain.models.Branding;
import com.youtube.channelservice.domain.models.Channel;
import com.youtube.channelservice.domain.models.Policy;
import com.youtube.channelservice.domain.models.Role;
import com.youtube.channelservice.infrastructure.persistence.entity.BrandingEmbeddable;
import com.youtube.channelservice.infrastructure.persistence.entity.ChannelEntity;
import com.youtube.channelservice.infrastructure.persistence.entity.ChannelMemberEntity;
import com.youtube.channelservice.infrastructure.persistence.entity.HandleEntity;
import com.youtube.channelservice.infrastructure.persistence.entity.PolicyEmbeddable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * MapStruct mapper for converting between domain models and JPA entities.
 * Provides type-safe mapping with automatic null handling.
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ChannelMapper {
    
    ChannelMapper INSTANCE = Mappers.getMapper(ChannelMapper.class);
    
    /**
     * Converts ChannelEntity to Channel domain model.
     * @param entity The JPA entity
     * @return The domain model
     */
    @Mapping(target = "etag", source = "etag")
    Channel toDomain(ChannelEntity entity);
    
    /**
     * Converts Channel domain model to ChannelEntity.
     * @param channel The domain model
     * @return The JPA entity
     */
    @Mapping(target = "etag", source = "etag")
    ChannelEntity toEntity(Channel channel);
    
    /**
     * Converts BrandingEmbeddable to Branding domain model.
     * @param embeddable The embeddable
     * @return The domain model
     */
    Branding toBrandingDomain(BrandingEmbeddable embeddable);
    
    /**
     * Converts Branding domain model to BrandingEmbeddable.
     * @param branding The domain model
     * @return The embeddable
     */
    BrandingEmbeddable toBrandingEmbeddable(Branding branding);
    
    /**
     * Converts PolicyEmbeddable to Policy domain model.
     * @param embeddable The embeddable
     * @return The domain model
     */
    Policy toPolicyDomain(PolicyEmbeddable embeddable);
    
    /**
     * Converts Policy domain model to PolicyEmbeddable.
     * @param policy The domain model
     * @return The embeddable
     */
    PolicyEmbeddable toPolicyEmbeddable(Policy policy);
    
    /**
     * Converts ChannelMemberEntity to Role.
     * @param entity The JPA entity
     * @return The role
     */
    Role toRole(ChannelMemberEntity entity);
    
    /**
     * Converts Role to ChannelMemberEntity.
     * @param role The role
     * @param channelId The channel ID
     * @param userId The user ID
     * @return The JPA entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "channelId", source = "channelId")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    ChannelMemberEntity toMemberEntity(Role role, String channelId, String userId);
    
    /**
     * Converts list of ChannelEntity to list of Channel domain models.
     * @param entities The JPA entities
     * @return The domain models
     */
    List<Channel> toDomainList(List<ChannelEntity> entities);
    
    /**
     * Converts list of ChannelMemberEntity to list of Role.
     * @param entities The JPA entities
     * @return The roles
     */
    List<Role> toRoleList(List<ChannelMemberEntity> entities);
}
