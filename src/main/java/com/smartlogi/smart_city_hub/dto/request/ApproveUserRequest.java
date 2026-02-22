package com.smartlogi.smart_city_hub.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for admin to approve a pending user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApproveUserRequest {

    private String notes; // Optional notes from admin
}
