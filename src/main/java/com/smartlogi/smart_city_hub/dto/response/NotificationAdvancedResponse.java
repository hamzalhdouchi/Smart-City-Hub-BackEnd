package com.smartlogi.smart_city_hub.dto.response;

import com.smartlogi.smart_city_hub.entity.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Advanced response DTO for notification (full details with incident).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationAdvancedResponse {

    private Long id;
    private String title;
    private String message;
    private NotificationType type;
    private Boolean read;
    private IncidentSimpleResponse incident;
    private LocalDateTime createdAt;
}
