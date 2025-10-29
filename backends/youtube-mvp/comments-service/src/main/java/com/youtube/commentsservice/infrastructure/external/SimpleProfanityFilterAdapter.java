package com.youtube.commentsservice.infrastructure.external;

import com.youtube.commentsservice.domain.services.ProfanityFilterPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Simple profanity filter adapter
 * In production, this could integrate with Azure Content Moderator
 */
@Slf4j
@Service
public class SimpleProfanityFilterAdapter implements ProfanityFilterPort {
    
    // Simple profanity word list - in production use Azure Content Moderator
    private static final Set<String> PROFANITY_WORDS = new HashSet<>(Arrays.asList(
            "spam", "badword", "inappropriate" // Add real words for production
    ));
    
    private static final Pattern WORD_BOUNDARY = Pattern.compile("\\b");
    
    @Override
    public boolean containsProfanity(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        String lowercase = text.toLowerCase();
        return PROFANITY_WORDS.stream()
                .anyMatch(lowercase::contains);
    }
    
    @Override
    public String filterProfanity(String text) {
        if (!containsProfanity(text)) {
            return text;
        }
        
        String filtered = text;
        for (String word : PROFANITY_WORDS) {
            filtered = filtered.replaceAll("(?i)" + Pattern.quote(word), "****");
        }
        
        log.warn("Profanity filtered from text: {} -> {}", text, filtered);
        return filtered;
    }
}

