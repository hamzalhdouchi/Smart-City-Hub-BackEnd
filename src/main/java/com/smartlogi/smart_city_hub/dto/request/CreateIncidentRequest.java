package com.smartlogi.smart_city_hub.dto.request;

import com.smartlogi.smart_city_hub.entity.enums.Priority;
import com.smartlogi.smart_city_hub.entity.enums.UrgencyLevel;
import com.smartlogi.smart_city_hub.entity.enums.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new incident.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateIncidentRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    private String address;

    private String district;

    @NotNull(message = "Category is required")
    private Long categoryId;

    private UrgencyLevel urgencyLevel;

    private Visibility visibility;

    private Priority priority;
}
