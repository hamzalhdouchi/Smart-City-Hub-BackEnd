package com.smartlogi.smart_city_hub.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignAgentRequest {
    
    @NotNull(message = "Agent ID is required")
    private Long agentId;
}
