package com.smartlogi.smart_city_hub.dto.request;

import com.smartlogi.smart_city_hub.entity.enums.UrgencyLevel;
import com.smartlogi.smart_city_hub.entity.enums.Visibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an incident.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateIncidentRequest {

    private String title;

    private String description;

    private Double latitude;

    private Double longitude;

    private String address;

    private String district;

    private Long categoryId;

    private UrgencyLevel urgencyLevel;

    private Visibility visibility;
}
