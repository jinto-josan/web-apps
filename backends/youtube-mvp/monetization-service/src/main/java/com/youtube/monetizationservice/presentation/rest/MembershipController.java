package com.youtube.monetizationservice.presentation.rest;

import com.youtube.monetizationservice.application.dto.CreateMembershipRequest;
import com.youtube.monetizationservice.application.dto.MembershipResponse;
import com.youtube.monetizationservice.application.usecase.MembershipUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * OSCAL REST controller for membership operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/memberships")
@RequiredArgsConstructor
@Validated
@Tag(name = "Memberships", description = "Membership subscription management")
public class MembershipController {
    
    private final MembershipUseCase membershipUseCase;
    
    @PostMapping("/subscribe")
    @Operation(summary = "Subscribe to a membership", 
               description = "Creates a new membership subscription with payment processing")
    public ResponseEntity<MembershipResponse> subscribe(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateMembershipRequest request) {
        String subscriberId = jwt.getClaimAsString("sub");
        
        log.info("Creating membership for subscriber: {}, request: {}", subscriberId, request);
        
        MembershipResponse response = membershipUseCase.subscribe(subscriberId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{membershipId}")
    @Operation(summary = "Get membership details",
               description = "Retrieves membership information by ID")
    public ResponseEntity<MembershipResponse> getMembership(
            @PathVariable @NotBlank String membershipId) {
        log.debug("Retrieving membership: {}", membershipId);
        
        MembershipResponse response = membershipUseCase.getMembership(membershipId);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(summary = "List memberships",
               description = "Lists memberships for a subscriber or channel")
    public ResponseEntity<List<MembershipResponse>> listMemberships(
            @RequestParam(required = false) String subscriberId,
            @RequestParam(required = false) String channelId) {
        log.debug("Listing memberships - subscriberId: {}, channelId: {}", subscriberId, channelId);
        
        List<MembershipResponse> responses;
        if (subscriberId != null) {
            responses = membershipUseCase.listSubscriberMemberships(subscriberId);
        } else if (channelId != null) {
            responses = membershipUseCase.listChannelMemberships(channelId);
        } else {
            throw new IllegalArgumentException("Either subscriberId or channelId must be provided");
        }
        
        return ResponseEntity.ok(responses);
    }
}

