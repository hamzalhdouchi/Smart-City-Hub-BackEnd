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
public class PhotoResponse {

    private String id;
    private String fileName;
    private String filePath;
    private String url;
    private Long fileSize;
    private String uploadedById;
    private String uploadedByName;
    private LocalDateTime createdAt;
}
