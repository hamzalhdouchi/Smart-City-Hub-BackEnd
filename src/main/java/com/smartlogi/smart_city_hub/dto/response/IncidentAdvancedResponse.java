package com.smartlogi.smart_city_hub.dto.response;

import com.smartlogi.smart_city_hub.entity.enums.IncidentStatus;
import com.smartlogi.smart_city_hub.entity.enums.UrgencyLevel;
import com.smartlogi.smart_city_hub.entity.enums.Visibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Advanced response DTO for incident (full details with related entities).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentAdvancedResponse {

    private Long id;
    private String incidentNumber;
    private String title;
    private String description;
    private Double latitude;
    private Double longitude;
    private String address;
    private String district;
    private IncidentStatus status;
    private UrgencyLevel urgencyLevel;
    private Visibility visibility;

    private UserSimpleResponse reporter;
    private UserSimpleResponse assignedAgent;
    private CategorySimpleResponse category;

    private List<PhotoSimpleResponse> photos;
    private List<CommentSimpleResponse> comments;
    private List<StatusHistoryResponse> statusHistory;
    private RatingSimpleResponse rating;

    private Integer commentsCount;
    private Integer photosCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
}
