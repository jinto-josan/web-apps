package com.youtube.experimentationservice.domain.services.impl;

import com.youtube.experimentationservice.domain.model.Experiment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BucketingServiceTest {
    private BucketingServiceImpl bucketingService;

    @BeforeEach
    void setUp() {
        bucketingService = new BucketingServiceImpl();
    }

    @Test
    void testComputeBucket_Deterministic() {
        int bucket1 = bucketingService.computeBucket("user1", "exp1");
        int bucket2 = bucketingService.computeBucket("user1", "exp1");
        
        assertEquals(bucket1, bucket2, "Bucket should be deterministic for same user and experiment");
        assertTrue(bucket1 >= 0 && bucket1 < 10000, "Bucket should be in range 0-9999");
    }

    @Test
    void testComputeBucket_DifferentUsers() {
        int bucket1 = bucketingService.computeBucket("user1", "exp1");
        int bucket2 = bucketingService.computeBucket("user2", "exp1");
        
        // Should likely be different (very unlikely to collide)
        assertNotEquals(bucket1, bucket2, "Different users should get different buckets");
    }

    @Test
    void testAssignVariant() {
        Experiment experiment = Experiment.builder()
                .key("test-exp")
                .variants(Arrays.asList(
                        Experiment.Variant.builder()
                                .id("variant1")
                                .name("Control")
                                .trafficPercentage(0.5)
                                .configuration(Map.of("feature", "off"))
                                .build(),
                        Experiment.Variant.builder()
                                .id("variant2")
                                .name("Treatment")
                                .trafficPercentage(0.5)
                                .configuration(Map.of("feature", "on"))
                                .build()))
                .build();

        Experiment.Variant variant = bucketingService.assignVariant("user1", experiment);
        
        assertNotNull(variant);
        assertTrue(variant.getId().equals("variant1") || variant.getId().equals("variant2"));
    }

    @Test
    void testAssignVariant_Deterministic() {
        Experiment experiment = Experiment.builder()
                .key("test-exp")
                .variants(Arrays.asList(
                        Experiment.Variant.builder()
                                .id("variant1")
                                .trafficPercentage(0.5)
                                .build(),
                        Experiment.Variant.builder()
                                .id("variant2")
                                .trafficPercentage(0.5)
                                .build()))
                .build();

        Experiment.Variant variant1 = bucketingService.assignVariant("user1", experiment);
        Experiment.Variant variant2 = bucketingService.assignVariant("user1", experiment);
        
        assertEquals(variant1.getId(), variant2.getId(), "Assignment should be deterministic");
    }
}

