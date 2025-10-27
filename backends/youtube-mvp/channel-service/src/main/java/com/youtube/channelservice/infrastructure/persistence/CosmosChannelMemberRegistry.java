package com.youtube.channelservice.infrastructure.persistence;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.youtube.channelservice.domain.models.Role;
import com.youtube.channelservice.domain.repositories.ChannelMemberRepository;

import java.time.Instant;
import java.util.Optional;

/**
 * Cosmos DB implementation of ChannelMemberRepository.
 * Handles channel member persistence with proper partitioning and concurrency control.
 */
public class CosmosChannelMemberRegistry implements ChannelMemberRepository {
    
    private final CosmosContainer members;
    
    public CosmosChannelMemberRegistry(CosmosContainer members) {
        this.members = members;
    }
    
    @Override
    public Optional<Role> roleOf(String channelId, String userId) {
        try {
            String memberId = createMemberId(channelId, userId);
            CosmosItemResponse<MemberDoc> resp = members.readItem(memberId, new PartitionKey(channelId), MemberDoc.class);
            return Optional.of(Role.valueOf(resp.getItem().role));
        } catch (CosmosException ex) {
            if (ex.getStatusCode() == 404) return Optional.empty();
            throw ex;
        }
    }
    
    @Override
    public void add(String channelId, String userId, Role role) {
        String memberId = createMemberId(channelId, userId);
        MemberDoc doc = new MemberDoc();
        doc.id = memberId;
        doc.channelId = channelId;
        doc.userId = userId;
        doc.role = role.name();
        doc.createdAt = Instant.now().toString();
        doc.updatedAt = Instant.now().toString();
        
        members.createItem(doc, new PartitionKey(channelId), new CosmosItemRequestOptions());
    }
    
    @Override
    public void remove(String channelId, String userId) {
        String memberId = createMemberId(channelId, userId);
        members.deleteItem(memberId, new PartitionKey(channelId), new CosmosItemRequestOptions());
    }
    
    @Override
    public Optional<Role> updateRole(String channelId, String userId, Role newRole) {
        String memberId = createMemberId(channelId, userId);
        
        try {
            // Try to read existing member
            CosmosItemResponse<MemberDoc> resp = members.readItem(memberId, new PartitionKey(channelId), MemberDoc.class);
            MemberDoc doc = resp.getItem();
            Role oldRole = Role.valueOf(doc.role);
            
            // Update the role
            doc.role = newRole.name();
            doc.updatedAt = Instant.now().toString();
            
            CosmosItemRequestOptions opts = new CosmosItemRequestOptions().setIfMatchETag(resp.getResponseHeaders().get("ETag"));
            members.replaceItem(doc, memberId, new PartitionKey(channelId), opts);
            
            return Optional.of(oldRole);
            
        } catch (CosmosException ex) {
            if (ex.getStatusCode() == 404) {
                // Member doesn't exist, create new one
                add(channelId, userId, newRole);
                return Optional.empty();
            }
            throw ex;
        }
    }
    
    private String createMemberId(String channelId, String userId) {
        return channelId + ":" + userId;
    }
    
    // Document structure for Cosmos DB
    static class MemberDoc {
        public String id;
        public String channelId;
        public String userId;
        public String role;
        public String createdAt;
        public String updatedAt;
    }
}