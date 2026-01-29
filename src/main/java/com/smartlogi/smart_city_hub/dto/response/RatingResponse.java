package com.smartlogi.smart_city_hub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingResponse {
    
    private Long id;
    private Integer stars;
    private String feedback;
    private Long userId;
    private String userName;
    private LocalDateTime createdAt;
}
