package com.smartlogi.smart_city_hub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Advanced response DTO for rating (full details with user).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingAdvancedResponse {

    private Long id;
    private Integer stars;
    private String feedback;
    private UserSimpleResponse user;
    private Long incidentId;
    private LocalDateTime createdAt;
}
