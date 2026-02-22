package com.smartlogi.smart_city_hub.controller;

import com.smartlogi.smart_city_hub.dto.response.ApiResponse;
import com.smartlogi.smart_city_hub.dto.response.IncidentPhotoResponse;
import com.smartlogi.smart_city_hub.service.IncidentPhotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/incidents/{incidentId}/photos")
@RequiredArgsConstructor
@Tag(name = "Incident Photos", description = "Incident photo management")
@SecurityRequirement(name = "Bearer Authentication")
public class IncidentPhotoController {

    private final IncidentPhotoService photoService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Upload photos", description = "Upload one or more photos for an incident")
    public ResponseEntity<ApiResponse<List<IncidentPhotoResponse>>> uploadPhotos(
            @PathVariable String incidentId,
            @RequestPart("files") List<MultipartFile> files) {

        List<IncidentPhotoResponse> responses = photoService.uploadPhotos(incidentId, files);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Photos uploaded successfully", responses));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List photos", description = "Get all photos for an incident")
    public ResponseEntity<ApiResponse<List<IncidentPhotoResponse>>> getPhotos(
            @PathVariable String incidentId) {

        List<IncidentPhotoResponse> responses = photoService.getPhotosByIncident(incidentId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{photoId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get photo", description = "Get a specific photo details")
    public ResponseEntity<ApiResponse<IncidentPhotoResponse>> getPhoto(
            @PathVariable String incidentId,
            @PathVariable String photoId) {

        IncidentPhotoResponse response = photoService.getPhoto(incidentId, photoId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{photoId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete photo", description = "Delete a photo (uploader or admin only)")
    public ResponseEntity<ApiResponse<Void>> deletePhoto(
            @PathVariable String incidentId,
            @PathVariable String photoId) {

        photoService.deletePhoto(incidentId, photoId);
        return ResponseEntity.ok(ApiResponse.success("Photo deleted successfully", null));
    }
}
