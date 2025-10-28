package com.youtube.recommendationsservice.infrastructure.services;

import com.youtube.recommendationsservice.domain.entities.RecommendedItem;
import com.youtube.recommendationsservice.domain.services.DiversityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CategoryDiversityService implements DiversityService {
    
    @Override
    public List<RecommendedItem> applyDiversityConstraints(List<RecommendedItem> items, int maxFromSameCategory) {
        log.debug("Applying diversity constraints, maxFromSameCategory: {}", maxFromSameCategory);
        
        if (items.isEmpty()) {
            return items;
        }
        
        // Group by category
        Map<String, List<RecommendedItem>> byCategory = items.stream()
            .collect(Collectors.groupingBy(item -> 
                item.getMetadata().getOrDefault("category", "unknown").toString()
            ));
        
        List<RecommendedItem> diversified = new ArrayList<>();
        Map<String, Integer> categoryCount = new HashMap<>();
        
        // Create a list of items with their categories shuffled
        List<RecommendedItem> allItems = new ArrayList<>(items);
        Collections.shuffle(allItems);
        
        for (RecommendedItem item : allItems) {
            String category = item.getMetadata().getOrDefault("category", "unknown").toString();
            categoryCount.putIfAbsent(category, 0);
            
            if (categoryCount.get(category) < maxFromSameCategory) {
                diversified.add(item);
                categoryCount.put(category, categoryCount.get(category) + 1);
            }
        }
        
        // Sort by score to maintain quality
        diversified.sort((a, b) -> Double.compare(b.getScore().getValue(), a.getScore().getValue()));
        
        log.debug("Diversified from {} to {} items", items.size(), diversified.size());
        return diversified;
    }
}

