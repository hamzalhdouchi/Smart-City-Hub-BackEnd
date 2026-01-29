package com.smartlogi.smart_city_hub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple response DTO for photo (minimal info).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoSimpleResponse {

    private Long id;
    private String fileName;
    private String fileUrl;
}
