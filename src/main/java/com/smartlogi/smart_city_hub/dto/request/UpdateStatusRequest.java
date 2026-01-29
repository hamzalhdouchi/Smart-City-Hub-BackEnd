package com.smartlogi.smart_city_hub.dto.request;

import com.smartlogi.smart_city_hub.entity.enums.IncidentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusRequest {
    
    @NotNull(message = "Status is required")
    private IncidentStatus status;
}
