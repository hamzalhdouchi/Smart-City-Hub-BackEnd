package com.smartlogi.smart_city_hub.dto.response;

import com.smartlogi.smart_city_hub.entity.enums.IncidentStatus;
import com.smartlogi.smart_city_hub.entity.enums.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentResponse {
    
    private Long id;
    private String title;
    private String description;
    private Double latitude;
    private Double longitude;
    private String address;
    private IncidentStatus status;
    private Priority priority;
    private UserResponse reporter;
    private UserResponse assignedAgent;
    private CategoryResponse category;
    private List<PhotoResponse> photos;
    private Integer commentsCount;
    private RatingResponse rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
}
