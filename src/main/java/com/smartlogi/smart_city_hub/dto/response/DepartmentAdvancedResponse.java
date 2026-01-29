package com.smartlogi.smart_city_hub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Advanced response DTO for department (full details with categories).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentAdvancedResponse {

    private Long id;
    private String name;
    private String description;
    private String email;
    private String phoneNumber;
    private List<CategorySimpleResponse> categories;
    private Integer categoriesCount;
    private Integer incidentsCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
