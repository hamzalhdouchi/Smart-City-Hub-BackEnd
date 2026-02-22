package com.smartlogi.smart_city_hub.entity.enums;

/**
 * User account status in the Smart City Hub system.
 * Used for the two-phase registration workflow.
 */
public enum UserStatus {
    PENDING, // Awaiting admin approval
    ACTIVE, // Approved and can login
    INACTIVE // Deactivated account
}
