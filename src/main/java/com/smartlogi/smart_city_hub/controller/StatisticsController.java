package com.smartlogi.smart_city_hub.controller;

import com.smartlogi.smart_city_hub.dto.response.ApiResponse;
import com.smartlogi.smart_city_hub.dto.response.StatisticsResponse;
import com.smartlogi.smart_city_hub.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "Analytics and statistics endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class StatisticsController {
    
    private final StatisticsService statisticsService;
    
    @GetMapping("/global")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Global statistics", description = "Get global platform statistics (Admin only)")
    public ResponseEntity<ApiResponse<StatisticsResponse>> getGlobalStatistics() {
        StatisticsResponse response = statisticsService.getGlobalStatistics();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/by-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    @Operation(summary = "Stats by status", description = "Get incident count by status")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStatsByStatus() {
        Map<String, Long> response = statisticsService.getIncidentsByStatus();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/by-category")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    @Operation(summary = "Stats by category", description = "Get incident count by category")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStatsByCategory() {
        Map<String, Long> response = statisticsService.getIncidentsByCategory();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
