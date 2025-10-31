package com.youtube.edgecdncontrol.domain.valueobjects;

import lombok.Value;

@Value
public class FrontDoorProfileId {
    String resourceGroup;
    String profileName;

    public FrontDoorProfileId(String resourceGroup, String profileName) {
        if (resourceGroup == null || resourceGroup.isBlank()) {
            throw new IllegalArgumentException("Resource group cannot be null or blank");
        }
        if (profileName == null || profileName.isBlank()) {
            throw new IllegalArgumentException("Profile name cannot be null or blank");
        }
        this.resourceGroup = resourceGroup;
        this.profileName = profileName;
    }

    public String getFullName() {
        return String.format("%s/%s", resourceGroup, profileName);
    }
}

