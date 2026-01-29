package com.smartlogi.smart_city_hub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple response DTO for category (minimal info).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorySimpleResponse {

    private Long id;
    private String name;
    private Boolean active;
}
