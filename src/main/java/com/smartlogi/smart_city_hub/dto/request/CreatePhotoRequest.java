package com.smartlogi.smart_city_hub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for photo upload metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePhotoRequest {

    @NotNull(message = "Incident ID is required")
    private Long incidentId;

    @NotBlank(message = "File name is required")
    private String fileName;

    @NotBlank(message = "File path is required")
    private String filePath;

    @NotBlank(message = "File URL is required")
    private String fileUrl;

    private Long fileSize;
}
