package com.smartlogi.smart_city_hub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsResponse {
    
    private Long totalIncidents;
    private Long totalUsers;
    private Long totalAgents;
    private Map<String, Long> incidentsByStatus;
    private Map<String, Long> incidentsByCategory;
    private Double averageRating;
    private Double averageResolutionTimeHours;
}
