package com.youtube.observabilityservice.application.service;

import com.youtube.observabilityservice.application.dto.*;
import com.youtube.observabilityservice.application.mappers.SyntheticCheckMapper;
import com.youtube.observabilityservice.domain.entities.SyntheticCheck;
import com.youtube.observabilityservice.domain.entities.SyntheticCheckResult;
import com.youtube.observabilityservice.domain.repositories.SyntheticCheckRepository;
import com.youtube.observabilityservice.domain.services.SyntheticCheckRunner;
import com.youtube.observabilityservice.domain.valueobjects.SyntheticCheckId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyntheticCheckApplicationService {
    
    private final SyntheticCheckRepository checkRepository;
    private final SyntheticCheckRunner checkRunner;
    private final SyntheticCheckMapper mapper;
    
    @Transactional
    public SyntheticCheckResponse createCheck(CreateSyntheticCheckRequest request) {
        SyntheticCheck check = mapper.toDomain(request);
        check.setId(SyntheticCheckId.random());
        check.setCreatedAt(Instant.now());
        check.setUpdatedAt(Instant.now());
        if (check.getEnabled() == null) {
            check.setEnabled(true);
        }
        
        SyntheticCheck saved = checkRepository.save(check);
        log.info("Created synthetic check: {}", saved.getId().getValue());
        
        return mapper.toResponse(saved);
    }
    
    public SyntheticCheckResponse getCheck(String checkId) {
        SyntheticCheck check = checkRepository.findById(SyntheticCheckId.from(checkId))
                .orElseThrow(() -> new IllegalArgumentException("Synthetic check not found: " + checkId));
        
        return mapper.toResponse(check);
    }
    
    public List<SyntheticCheckResponse> getAllChecks() {
        return checkRepository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }
    
    @Transactional
    public SyntheticCheckResultResponse runCheck(String checkId) {
        SyntheticCheck check = checkRepository.findById(SyntheticCheckId.from(checkId))
                .orElseThrow(() -> new IllegalArgumentException("Synthetic check not found: " + checkId));
        
        log.info("Running synthetic check: {}", checkId);
        
        SyntheticCheckResult result = checkRunner.run(check);
        
        check.setLastRunAt(Instant.now());
        check.setLastResult(result);
        check.setUpdatedAt(Instant.now());
        
        checkRepository.save(check);
        
        log.info("Synthetic check {} completed: success={}, responseTime={}ms", 
                checkId, result.getSuccess(), result.getResponseTimeMs());
        
        return mapper.toResultResponse(result);
    }
    
    @Transactional
    public void enableCheck(String checkId) {
        SyntheticCheck check = checkRepository.findById(SyntheticCheckId.from(checkId))
                .orElseThrow(() -> new IllegalArgumentException("Synthetic check not found: " + checkId));
        
        check.setEnabled(true);
        check.setUpdatedAt(Instant.now());
        checkRepository.save(check);
    }
    
    @Transactional
    public void disableCheck(String checkId) {
        SyntheticCheck check = checkRepository.findById(SyntheticCheckId.from(checkId))
                .orElseThrow(() -> new IllegalArgumentException("Synthetic check not found: " + checkId));
        
        check.setEnabled(false);
        check.setUpdatedAt(Instant.now());
        checkRepository.save(check);
    }
    
    @Transactional
    public void deleteCheck(String checkId) {
        checkRepository.deleteById(SyntheticCheckId.from(checkId));
        log.info("Deleted synthetic check: {}", checkId);
    }
}

