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
public class PhotoAdvancedResponse {

    private Long id;
    private String fileName;
    private String filePath;
    private String fileUrl;
    private Long fileSize;
    private UserSimpleResponse uploadedBy;
    private Long incidentId;
    private LocalDateTime uploadedAt;
}
