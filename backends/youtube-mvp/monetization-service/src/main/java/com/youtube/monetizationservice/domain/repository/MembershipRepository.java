package com.youtube.monetizationservice.domain.repository;

import com.youtube.monetizationservice.domain.models.Membership;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Membership aggregate root.
 * Defines the contract for membership persistence operations.
 */
public interface MembershipRepository {
    
    Membership save(Membership membership);
    
    Optional<Membership> findById(String membershipId);
    
    List<Membership> findByChannelId(String channelId);
    
    List<Membership> findBySubscriberId(String subscriberId);
    
    Optional<Membership> findByChannelIdAndSubscriberId(String channelId, String subscriberId);
    
    boolean existsById(String membershipId);
    
    void deleteById(String membershipId);
}

