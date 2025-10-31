package com.youtube.observabilityservice.infrastructure.scheduler;

import com.youtube.observabilityservice.application.service.SyntheticCheckApplicationService;
import com.youtube.observabilityservice.domain.repositories.SyntheticCheckRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class SyntheticCheckScheduler {
    
    private final SyntheticCheckRepository checkRepository;
    private final SyntheticCheckApplicationService checkApplicationService;
    
    @Scheduled(fixedRateString = "${synthetic.check.interval-ms:60000}") // Default 1 minute
    public void runScheduledChecks() {
        log.debug("Starting scheduled synthetic checks");
        
        checkRepository.findByEnabled(true).forEach(check -> {
            try {
                // Check if it's time to run this check
                if (shouldRunCheck(check)) {
                    checkApplicationService.runCheck(check.getId().getValue().toString());
                }
            } catch (Exception e) {
                log.error("Error running synthetic check {}: {}", 
                        check.getId().getValue(), e.getMessage(), e);
            }
        });
        
        log.debug("Completed scheduled synthetic checks");
    }
    
    private boolean shouldRunCheck(com.youtube.observabilityservice.domain.entities.SyntheticCheck check) {
        if (check.getLastRunAt() == null) {
            return true;
        }
        
        long secondsSinceLastRun = ChronoUnit.SECONDS.between(
                check.getLastRunAt(), Instant.now());
        
        return secondsSinceLastRun >= check.getIntervalSeconds();
    }
}

