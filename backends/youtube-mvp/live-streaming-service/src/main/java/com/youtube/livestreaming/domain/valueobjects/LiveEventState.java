package com.youtube.livestreaming.domain.valueobjects;

/**
 * Live event lifecycle states
 */
public enum LiveEventState {
    CREATED,
    STARTING,
    RUNNING,
    STOPPING,
    STOPPED,
    ARCHIVING,
    ARCHIVED,
    FAILED,
    DELETED;
    
    public boolean canTransitionTo(LiveEventState newState) {
        return switch (this) {
            case CREATED -> newState == STARTING || newState == DELETED || newState == FAILED;
            case STARTING -> newState == RUNNING || newState == FAILED || newState == STOPPED;
            case RUNNING -> newState == STOPPING || newState == FAILED;
            case STOPPING -> newState == STOPPED || newState == FAILED || newState == ARCHIVING;
            case STOPPED -> newState == STARTING || newState == ARCHIVING || newState == ARCHIVED || newState == DELETED;
            case ARCHIVING -> newState == ARCHIVED || newState == FAILED;
            case ARCHIVED -> newState == DELETED;
            case FAILED -> newState == DELETED;
            case DELETED -> false;
        };
    }
}

