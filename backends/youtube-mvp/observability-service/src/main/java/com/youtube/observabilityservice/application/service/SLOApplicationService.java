package com.youtube.observabilityservice.application.service;

import com.youtube.observabilityservice.application.dto.CreateSLORequest;
import com.youtube.observabilityservice.application.dto.SLOResponse;
import com.youtube.observabilityservice.application.mappers.SLOMapper;
import com.youtube.observabilityservice.domain.entities.SLI;
import com.youtube.observabilityservice.domain.entities.SLO;
import com.youtube.observabilityservice.domain.repositories.SLORepository;
import com.youtube.observabilityservice.domain.services.AzureMonitorQueryPort;
import com.youtube.observabilityservice.domain.services.SLOCalculator;
import com.youtube.observabilityservice.domain.services.SLOExporterPort;
import com.youtube.observabilityservice.domain.valueobjects.SLOId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SLOApplicationService {
    
    private final SLORepository sloRepository;
    private final SLOCalculator sloCalculator;
    private final AzureMonitorQueryPort azureMonitorQueryPort;
    private final SLOExporterPort sloExporterPort;
    private final SLOMapper sloMapper;
    
    @Transactional
    public SLOResponse createSLO(CreateSLORequest request) {
        SLO slo = sloMapper.toDomain(request);
        slo.setId(SLOId.random());
        slo.setCreatedAt(Instant.now());
        slo.setUpdatedAt(Instant.now());
        slo.setErrorBudget(slo.calculateErrorBudget());
        
        SLO saved = sloRepository.save(slo);
        log.info("Created SLO: {}", saved.getId().getValue());
        
        // Calculate initial SLO value
        double currentSLO = calculateCurrentSLO(saved);
        double burnRate = 0.0; // No previous value
        
        return sloMapper.toResponse(saved, currentSLO, burnRate);
    }
    
    public SLOResponse getSLO(String sloId) {
        SLO slo = sloRepository.findById(SLOId.from(sloId))
                .orElseThrow(() -> new IllegalArgumentException("SLO not found: " + sloId));
        
        double currentSLO = calculateCurrentSLO(slo);
        double burnRate = calculateBurnRate(slo, currentSLO);
        
        return sloMapper.toResponse(slo, currentSLO, burnRate);
    }
    
    public List<SLOResponse> getAllSLOs() {
        return sloRepository.findAll().stream()
                .map(slo -> {
                    double currentSLO = calculateCurrentSLO(slo);
                    double burnRate = calculateBurnRate(slo, currentSLO);
                    return sloMapper.toResponse(slo, currentSLO, burnRate);
                })
                .toList();
    }
    
    public List<SLOResponse> getSLOsByService(String serviceName) {
        return sloRepository.findByServiceName(serviceName).stream()
                .map(slo -> {
                    double currentSLO = calculateCurrentSLO(slo);
                    double burnRate = calculateBurnRate(slo, currentSLO);
                    return sloMapper.toResponse(slo, currentSLO, burnRate);
                })
                .toList();
    }
    
    @Transactional
    public void recalculateSLO(String sloId) {
        SLO slo = sloRepository.findById(SLOId.from(sloId))
                .orElseThrow(() -> new IllegalArgumentException("SLO not found: " + sloId));
        
        double currentSLO = calculateCurrentSLO(slo);
        
        // Update SLI values
        List<SLI> updatedSLIs = new ArrayList<>();
        for (SLI sli : slo.getSlis()) {
            Double newValue = azureMonitorQueryPort.calculateSLI(sli);
            if (newValue != null) {
                sli.setLastValue(newValue);
                sli.setLastCalculatedAt(Instant.now());
            }
            updatedSLIs.add(sli);
        }
        slo.setSlis(updatedSLIs);
        slo.setErrorBudgetRemaining(slo.calculateRemainingErrorBudget(currentSLO));
        slo.setUpdatedAt(Instant.now());
        
        sloRepository.save(slo);
        
        // Export to monitoring systems
        sloExporterPort.export(slo);
        
        log.info("Recalculated SLO {}: current={}", sloId, currentSLO);
    }
    
    private double calculateCurrentSLO(SLO slo) {
        List<SLI> currentSLIs = new ArrayList<>();
        for (SLI sli : slo.getSlis()) {
            Double value = azureMonitorQueryPort.calculateSLI(sli);
            if (value != null) {
                sli.setLastValue(value);
                sli.setLastCalculatedAt(Instant.now());
                currentSLIs.add(sli);
            }
        }
        return sloCalculator.calculateSLO(slo, currentSLIs);
    }
    
    private double calculateBurnRate(SLO slo, double currentSLO) {
        // Simplified: would need to store previous SLO value
        // For now, return 0 if no previous value exists
        return 0.0;
    }
}

