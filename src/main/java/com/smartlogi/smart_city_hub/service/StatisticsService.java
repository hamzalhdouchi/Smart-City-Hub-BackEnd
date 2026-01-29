package com.smartlogi.smart_city_hub.service;

import com.smartlogi.smart_city_hub.dto.response.StatisticsResponse;
import com.smartlogi.smart_city_hub.entity.enums.Role;
import com.smartlogi.smart_city_hub.repository.IncidentRepository;
import com.smartlogi.smart_city_hub.repository.RatingRepository;
import com.smartlogi.smart_city_hub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {
    
    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;
    
    public StatisticsResponse getGlobalStatistics() {
        // Get counts
        long totalIncidents = incidentRepository.count();
        long totalUsers = userRepository.count();
        long totalAgents = userRepository.findByRole(Role.ROLE_AGENT).size();
        
        // Get incidents by status
        Map<String, Long> incidentsByStatus = new HashMap<>();
        incidentRepository.countByStatusGrouped().forEach(row -> {
            incidentsByStatus.put(row[0].toString(), (Long) row[1]);
        });
        
        // Get incidents by category
        Map<String, Long> incidentsByCategory = new HashMap<>();
        incidentRepository.countByCategoryGrouped().forEach(row -> {
            incidentsByCategory.put((String) row[0], (Long) row[1]);
        });
        
        // Get average rating
        Double averageRating = ratingRepository.getAverageRating();
        
        return StatisticsResponse.builder()
                .totalIncidents(totalIncidents)
                .totalUsers(totalUsers)
                .totalAgents(totalAgents)
                .incidentsByStatus(incidentsByStatus)
                .incidentsByCategory(incidentsByCategory)
                .averageRating(averageRating != null ? averageRating : 0.0)
                .averageResolutionTimeHours(0.0) // TODO: Calculate this
                .build();
    }
    
    public Map<String, Long> getIncidentsByStatus() {
        Map<String, Long> result = new HashMap<>();
        incidentRepository.countByStatusGrouped().forEach(row -> {
            result.put(row[0].toString(), (Long) row[1]);
        });
        return result;
    }
    
    public Map<String, Long> getIncidentsByCategory() {
        Map<String, Long> result = new HashMap<>();
        incidentRepository.countByCategoryGrouped().forEach(row -> {
            result.put((String) row[0], (Long) row[1]);
        });
        return result;
    }
}
