package com.smartlogi.smart_city_hub.entity.enums;

/**
 * Incident status lifecycle in the Smart City Hub system.
 */
public enum IncidentStatus {
    NEW, // Initial state - incident just created
    ASSIGNED, // Assigned to an agent
    IN_PROGRESS, // Being handled by an agent
    PENDING_VALIDATION, // Agent submitted resolution with photo, awaiting admin approval
    RESOLVED, // Admin approved the resolution
    VALIDATED, // Confirmed/validated
    REJECTED, // Invalid or duplicate report
    REOPENED // Reopened after resolution
}
