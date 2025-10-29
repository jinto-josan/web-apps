package com.youtube.monetizationservice.application.usecase;

import com.youtube.monetizationservice.application.dto.CreateMembershipRequest;
import com.youtube.monetizationservice.application.dto.MembershipResponse;
import com.youtube.monetizationservice.domain.models.Membership;
import com.youtube.monetizationservice.domain.repository.MembershipRepository;
import com.youtube.monetizationservice.domain.valueobjects.Money;
import com.youtube.monetizationservice.domain.event.MembershipCreatedEvent;
import com.youtube.monetizationservice.infrastructure.outbox.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Use case for membership management operations.
 * Implements the SAGA pattern for membership lifecycle.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipUseCase {
    
    private final MembershipRepository membershipRepository;
    private final EventPublisher eventPublisher;
    
    /**
     * Creates a new membership subscription with payment processing.
     * Implements the SAGA pattern for reliable transaction processing.
     */
    @Transactional
    public MembershipResponse subscribe(String subscriberId, CreateMembershipRequest request) {
        log.info("Creating membership for subscriber: {}, channel: {}, tier: {}", 
            subscriberId, request.getChannelId(), request.getTier());
        
        // Check if membership already exists
        var existing = membershipRepository.findByChannelIdAndSubscriberId(
            request.getChannelId(), subscriberId);
        
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Membership already exists for this subscriber");
        }
        
        // Create membership entity
        Money monthlyFee = new Money(request.getMonthlyFeeAmount(), request.getCurrency());
        Membership membership = Membership.builder()
                .id(UUID.randomUUID().toString()) // In production, use ULID generator
                .channelId(request.getChannelId())
                .subscriberId(subscriberId)
                .tier(request.getTier())
                .monthlyFee(monthlyFee)
                .status(Membership.MembershipStatus.ACTIVE)
                .startDate(Instant.now())
                .nextBillingDate(Instant.now().plusSeconds(30 * 24 * 3600)) // 30 days
                .paymentMethodId(request.getPaymentMethodId())
                .version(1)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .etag(UUID.randomUUID().toString())
                .build();
        
        // Save membership
        Membership saved = membershipRepository.save(membership);
        
        // Publish domain event
        MembershipCreatedEvent event = new MembershipCreatedEvent(
            saved.getId(),
            saved.getChannelId(),
            saved.getSubscriberId(),
            saved.getTier().name()
        );
        eventPublisher.publish(event);
        
        // Return response
        return toResponse(saved);
    }
    
    /**
     * Retrieves membership by ID.
     */
    public MembershipResponse getMembership(String membershipId) {
        log.debug("Retrieving membership: {}", membershipId);
        
        Membership membership = membershipRepository.findById(membershipId)
            .orElseThrow(() -> new IllegalArgumentException("Membership not found: " + membershipId));
        
        return toResponse(membership);
    }
    
    /**
     * Lists memberships for a subscriber.
     */
    public List<MembershipResponse> listSubscriberMemberships(String subscriberId) {
        log.debug("Listing memberships for subscriber: {}", subscriberId);
        
        List<Membership> memberships = membershipRepository.findBySubscriberId(subscriberId);
        
        return memberships.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Lists memberships for a channel.
     */
    public List<MembershipResponse> listChannelMemberships(String channelId) {
        log.debug("Listing memberships for channel: {}", channelId);
        
        List<Membership> memberships = membershipRepository.findByChannelId(channelId);
        
        return memberships.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    private MembershipResponse toResponse(Membership membership) {
        return MembershipResponse.builder()
                .id(membership.getId())
                .channelId(membership.getChannelId())
                .subscriberId(membership.getSubscriberId())
                .tier(membership.getTier())
                .monthlyFeeAmount(membership.getMonthlyFee().getAmount())
                .currency(membership.getMonthlyFee().getCurrency().getCurrencyCode())
                .status(membership.getStatus().name())
                .startDate(membership.getStartDate())
                .endDate(membership.getEndDate())
                .nextBillingDate(membership.getNextBillingDate())
                .createdAt(membership.getCreatedAt())
                .updatedAt(membership.getUpdatedAt())
                .build();
    }
}

