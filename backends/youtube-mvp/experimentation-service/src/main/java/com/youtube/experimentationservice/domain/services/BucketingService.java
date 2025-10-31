package com.youtube.experimentationservice.domain.services;

import com.youtube.experimentationservice.domain.model.Experiment;

public interface BucketingService {
    /**
     * Deterministically assign a user to a bucket (0-9999) based on userId and experimentKey.
     */
    int computeBucket(String userId, String experimentKey);

    /**
     * Assign user to a variant based on experiment configuration.
     */
    Experiment.Variant assignVariant(String userId, Experiment experiment);
}

