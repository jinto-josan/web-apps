package com.youtube.channelservice.infrastructure.services;

import com.youtube.channelservice.domain.services.ReservedWordsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Infrastructure implementation of ReservedWordsService.
 * Provides validation against reserved words and system handles.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservedWordsServiceImpl implements ReservedWordsService {
    
    private final Set<String> reservedHandles;
    
    public ReservedWordsServiceImpl(@org.springframework.beans.factory.annotation.Value("${handles.reserved-words}") Set<String> reservedHandles) {
        this.reservedHandles = Set.copyOf(reservedHandles);
    }
    
    @Override
    public boolean isReserved(String handleLower) {
        boolean reserved = reservedHandles.contains(handleLower);
        if (reserved) {
            log.debug("Handle '{}' is reserved", handleLower);
        }
        return reserved;
    }
    
    @Override
    public Set<String> getReservedHandles() {
        return Set.copyOf(reservedHandles);
    }
}
