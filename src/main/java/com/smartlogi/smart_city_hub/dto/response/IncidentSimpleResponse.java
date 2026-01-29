package com.smartlogi.smart_city_hub.dto.response;

import com.smartlogi.smart_city_hub.entity.enums.IncidentStatus;
import com.smartlogi.smart_city_hub.entity.enums.UrgencyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Simple response DTO for incident (minimal info for lists).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentSimpleResponse {

    private Long id;
    private String incidentNumber;
    private String title;
    private IncidentStatus status;
    private UrgencyLevel urgencyLevel;
    private String categoryName;
    private LocalDateTime createdAt;
}
