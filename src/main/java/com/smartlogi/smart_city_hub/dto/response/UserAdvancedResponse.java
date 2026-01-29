package com.smartlogi.smart_city_hub.dto.response;

import com.smartlogi.smart_city_hub.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Advanced response DTO for user (full details with statistics).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAdvancedResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phone;
    private Role role;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Statistics
    private Integer incidentsReported;
    private Integer incidentsAssigned;
    private Integer incidentsResolved;
    private Double averageRating;
}
