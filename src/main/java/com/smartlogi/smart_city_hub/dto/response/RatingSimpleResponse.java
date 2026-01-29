package com.smartlogi.smart_city_hub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Simple response DTO for rating (minimal info).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingSimpleResponse {

    private Long id;
    private Integer stars;
    private LocalDateTime createdAt;
}
