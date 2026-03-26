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
public class CommentAdvancedResponse {

    private Long id;
    private String content;
    private Boolean isInternal;
    private UserSimpleResponse author;
    private Long incidentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
