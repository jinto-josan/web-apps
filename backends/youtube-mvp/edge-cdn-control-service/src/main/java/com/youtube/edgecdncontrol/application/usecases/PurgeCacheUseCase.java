package com.youtube.edgecdncontrol.application.usecases;

import com.youtube.edgecdncontrol.application.dto.PurgeRequestDto;
import com.youtube.edgecdncontrol.application.dto.PurgeResponse;
import com.youtube.edgecdncontrol.domain.entities.PurgeRequest;
import com.youtube.edgecdncontrol.domain.repositories.PurgeRequestRepository;
import com.youtube.edgecdncontrol.domain.services.AzureFrontDoorPort;
import com.youtube.edgecdncontrol.domain.valueobjects.FrontDoorProfileId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurgeCacheUseCase {
    
    private final PurgeRequestRepository purgeRequestRepository;
    private final AzureFrontDoorPort azureFrontDoorPort;
    
    @Transactional
    public PurgeResponse execute(PurgeRequestDto request, String requestedBy) {
        log.info("Creating purge request for profile: {}/{}", 
                request.getResourceGroup(), request.getFrontDoorProfileName());
        
        FrontDoorProfileId profileId = new FrontDoorProfileId(
                request.getResourceGroup(), 
                request.getFrontDoorProfileName());
        
        PurgeRequest purgeRequest = PurgeRequest.builder()
                .id(UUID.randomUUID())
                .frontDoorProfile(profileId)
                .contentPaths(request.getContentPaths())
                .purgeType(mapPurgeType(request.getPurgeType()))
                .requestedBy(requestedBy)
                .requestedAt(Instant.now())
                .status(PurgeRequest.PurgeStatus.PENDING)
                .build();
        
        PurgeRequest saved = purgeRequestRepository.save(purgeRequest);
        
        // Execute purge asynchronously (could be done via Service Bus)
        try {
            azureFrontDoorPort.purgeCache(profileId, request.getContentPaths(), mapPurgeType(request.getPurgeType()));
            PurgeRequest completed = PurgeRequest.builder()
                    .id(saved.getId())
                    .frontDoorProfile(saved.getFrontDoorProfile())
                    .contentPaths(saved.getContentPaths())
                    .purgeType(saved.getPurgeType())
                    .requestedBy(saved.getRequestedBy())
                    .requestedAt(saved.getRequestedAt())
                    .status(PurgeRequest.PurgeStatus.COMPLETED)
                    .build();
            PurgeRequest finalSaved = purgeRequestRepository.save(completed);
            log.info("Purge request completed: {}", finalSaved.getId());
            return toResponse(finalSaved);
        } catch (Exception e) {
            log.error("Purge request failed: {}", saved.getId(), e);
            PurgeRequest failed = PurgeRequest.builder()
                    .id(saved.getId())
                    .frontDoorProfile(saved.getFrontDoorProfile())
                    .contentPaths(saved.getContentPaths())
                    .purgeType(saved.getPurgeType())
                    .requestedBy(saved.getRequestedBy())
                    .requestedAt(saved.getRequestedAt())
                    .status(PurgeRequest.PurgeStatus.FAILED)
                    .errorMessage(e.getMessage())
                    .build();
            PurgeRequest finalSaved = purgeRequestRepository.save(failed);
            throw new RuntimeException("Purge failed: " + e.getMessage(), e);
        }
    }
    
    private PurgeRequest.PurgeType mapPurgeType(PurgeRequestDto.PurgeType type) {
        return switch (type) {
            case SINGLE_PATH -> PurgeRequest.PurgeType.SINGLE_PATH;
            case WILDCARD -> PurgeRequest.PurgeType.WILDCARD;
            case ALL -> PurgeRequest.PurgeType.ALL;
        };
    }
    
    private PurgeResponse toResponse(PurgeRequest request) {
        return PurgeResponse.builder()
                .id(request.getId())
                .resourceGroup(request.getFrontDoorProfile().getResourceGroup())
                .frontDoorProfileName(request.getFrontDoorProfile().getProfileName())
                .contentPaths(request.getContentPaths())
                .purgeType(request.getPurgeType())
                .requestedBy(request.getRequestedBy())
                .requestedAt(request.getRequestedAt())
                .status(request.getStatus())
                .errorMessage(request.getErrorMessage())
                .build();
    }
}

