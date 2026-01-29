package com.smartlogi.smart_city_hub.controller;

import com.smartlogi.smart_city_hub.dto.request.AssignAgentRequest;
import com.smartlogi.smart_city_hub.dto.request.CreateIncidentRequest;
import com.smartlogi.smart_city_hub.dto.request.UpdateStatusRequest;
import com.smartlogi.smart_city_hub.dto.response.ApiResponse;
import com.smartlogi.smart_city_hub.dto.response.IncidentResponse;
import com.smartlogi.smart_city_hub.entity.enums.IncidentStatus;
import com.smartlogi.smart_city_hub.service.IncidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
@Tag(name = "Incidents", description = "Incident management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class IncidentController {

    private final IncidentService incidentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'AGENT', 'ADMIN')")
    @Operation(summary = "Create incident", description = "Report a new urban incident")
    public ResponseEntity<ApiResponse<IncidentResponse>> createIncident(
            @Valid @RequestBody CreateIncidentRequest request) {
        IncidentResponse response = incidentService.createIncident(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Incident reported", response));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List incidents", description = "Get all incidents with optional filters")
    public ResponseEntity<ApiResponse<Page<IncidentResponse>>> getIncidents(
            @RequestParam(required = false) IncidentStatus status,
            @RequestParam(required = false) Long categoryId,
            Pageable pageable) {
        Page<IncidentResponse> response = incidentService.getAllIncidents(status, categoryId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get incident", description = "Get incident details by ID")
    public ResponseEntity<ApiResponse<IncidentResponse>> getIncidentById(@PathVariable Long id) {
        IncidentResponse response = incidentService.getIncidentById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "My incidents", description = "Get incidents reported by current user")
    public ResponseEntity<ApiResponse<Page<IncidentResponse>>> getMyIncidents(Pageable pageable) {
        Page<IncidentResponse> response = incidentService.getMyIncidents(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/assigned")
    @PreAuthorize("hasRole('AGENT')")
    @Operation(summary = "Assigned incidents", description = "Get incidents assigned to current agent")
    public ResponseEntity<ApiResponse<Page<IncidentResponse>>> getAssignedIncidents(Pageable pageable) {
        Page<IncidentResponse> response = incidentService.getAssignedIncidents(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('AGENT', 'SUPERVISOR', 'ADMIN')")
    @Operation(summary = "Update status", description = "Change incident status")
    public ResponseEntity<ApiResponse<IncidentResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {
        IncidentResponse response = incidentService.updateStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Status updated", response));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMIN')")
    @Operation(summary = "Assign agent", description = "Assign an agent to handle the incident")
    public ResponseEntity<ApiResponse<IncidentResponse>> assignAgent(
            @PathVariable Long id,
            @Valid @RequestBody AssignAgentRequest request) {
        IncidentResponse response = incidentService.assignAgent(id, request);
        return ResponseEntity.ok(ApiResponse.success("Agent assigned", response));
    }
}
