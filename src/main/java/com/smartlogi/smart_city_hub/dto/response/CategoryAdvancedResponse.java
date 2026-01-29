package com.smartlogi.smart_city_hub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Advanced response DTO for category (full details with department).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryAdvancedResponse {

    private Long id;
    private String name;
    private String description;
    private Boolean active;
    private DepartmentSimpleResponse department;
    private Integer incidentCount;
    private LocalDateTime createdAt;
}
