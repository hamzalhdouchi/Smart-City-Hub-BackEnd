package com.smartlogi.smart_city_hub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Simple response DTO for comment (minimal info).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentSimpleResponse {

    private Long id;
    private String content;
    private String authorName;
    private Boolean isInternal;
    private LocalDateTime createdAt;
}
