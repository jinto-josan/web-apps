package com.youtube.channelservice.domain.models;

/**
 * Represents the role of a user in a channel.
 * Enumeration of possible roles with hierarchical permissions.
 */
public enum Role {
    
    /**
     * Owner role - highest level of access, can manage all aspects of the channel
     */
    OWNER("owner"),
    
    /**
     * Manager role - can manage content and members, but not ownership
     */
    MANAGER("manager"),
    
    /**
     * Editor role - can create and edit content
     */
    EDITOR("editor"),
    
    /**
     * Viewer role - can only view content
     */
    VIEWER("viewer");
    
    private final String value;
    
    Role(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    /**
     * Checks if this role has permission to perform the given action.
     * @param action The action to check permission for
     * @return true if the role has permission, false otherwise
     */
    public boolean hasPermission(String action) {
        return switch (this) {
            case OWNER -> true; // Owner can do everything
            case MANAGER -> !"transfer_ownership".equals(action) && !"delete_channel".equals(action);
            case EDITOR -> "create_content".equals(action) || "edit_content".equals(action) || "view_content".equals(action);
            case VIEWER -> "view_content".equals(action);
        };
    }
    
    /**
     * Checks if this role can manage the given target role.
     * @param targetRole The role to check if this role can manage
     * @return true if this role can manage the target role, false otherwise
     */
    public boolean canManage(Role targetRole) {
        return switch (this) {
            case OWNER -> targetRole != OWNER; // Owner can manage everyone except other owners
            case MANAGER -> targetRole == EDITOR || targetRole == VIEWER;
            case EDITOR, VIEWER -> false; // Editors and viewers cannot manage roles
        };
    }
    
    @Override
    public String toString() {
        return value;
    }
}