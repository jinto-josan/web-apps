package com.youtube.experimentationservice.domain.services.impl;

import com.youtube.experimentationservice.domain.model.Experiment;
import com.youtube.experimentationservice.domain.services.BucketingService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class BucketingServiceImpl implements BucketingService {

    @Override
    public int computeBucket(String userId, String experimentKey) {
        String combined = userId + ":" + experimentKey;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(combined.getBytes(StandardCharsets.UTF_8));
            long value = ((long) (hash[0] & 0xFF) << 24) |
                        ((long) (hash[1] & 0xFF) << 16) |
                        ((long) (hash[2] & 0xFF) << 8) |
                        ((long) (hash[3] & 0xFF));
            return (int) (Math.abs(value) % 10000);
        } catch (NoSuchAlgorithmException e) {
            // Fallback to hashCode
            return Math.abs(combined.hashCode()) % 10000;
        }
    }

    @Override
    public Experiment.Variant assignVariant(String userId, Experiment experiment) {
        int bucket = computeBucket(userId, experiment.getKey());
        double cumulative = 0.0;
        
        for (Experiment.Variant variant : experiment.getVariants()) {
            cumulative += variant.getTrafficPercentage() * 10000;
            if (bucket < cumulative) {
                return variant;
            }
        }
        
        // Fallback to first variant
        return experiment.getVariants().get(0);
    }
}

