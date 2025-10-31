package com.youtube.observabilityservice.infrastructure.scheduler;

import com.youtube.observabilityservice.application.service.SLOApplicationService;
import com.youtube.observabilityservice.domain.repositories.SLORepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SLIRecalculatorScheduler {
    
    private final SLORepository sloRepository;
    private final SLOApplicationService sloApplicationService;
    
    @Scheduled(fixedRateString = "${slo.recalculation.interval-ms:300000}") // Default 5 minutes
    public void recalculateAllSLOs() {
        log.info("Starting scheduled SLO recalculation");
        
        sloRepository.findAll().forEach(slo -> {
            try {
                sloApplicationService.recalculateSLO(slo.getId().getValue().toString());
            } catch (Exception e) {
                log.error("Error recalculating SLO {}: {}", slo.getId().getValue(), e.getMessage(), e);
            }
        });
        
        log.info("Completed scheduled SLO recalculation");
    }
}

