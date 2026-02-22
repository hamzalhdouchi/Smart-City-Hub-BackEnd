package com.smartlogi.smart_city_hub.service;

import com.smartlogi.smart_city_hub.dto.response.IncidentPhotoResponse;
import com.smartlogi.smart_city_hub.entity.Incident;
import com.smartlogi.smart_city_hub.entity.IncidentPhoto;
import com.smartlogi.smart_city_hub.entity.User;
import com.smartlogi.smart_city_hub.exception.ForbiddenException;
import com.smartlogi.smart_city_hub.exception.ResourceNotFoundException;
import com.smartlogi.smart_city_hub.repository.IncidentPhotoRepository;
import com.smartlogi.smart_city_hub.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncidentPhotoService {

    private final IncidentPhotoRepository photoRepository;
    private final IncidentRepository incidentRepository;
    private final MinioService minioService;
    private final UserService userService;

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp");

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @Transactional
    public List<IncidentPhotoResponse> uploadPhotos(String incidentId, List<MultipartFile> files) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", "id", incidentId));

        User currentUser = userService.getCurrentUser();

        List<IncidentPhotoResponse> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            validateFile(file);

            try {
                String folder = "incidents/" + incidentId;
                String objectName = minioService.uploadFile(file, folder);
                String fileUrl = minioService.getPresignedUrl(objectName);

                IncidentPhoto photo = IncidentPhoto.builder()
                        .incident(incident)
                        .fileName(file.getOriginalFilename())
                        .filePath(objectName)
                        .fileUrl(fileUrl)
                        .fileSize(file.getSize())
                        .uploadedBy(currentUser)
                        .build();

                photo = photoRepository.save(photo);
                log.info("Photo uploaded for incident {}: {}", incidentId, objectName);

                responses.add(toResponse(photo));
            } catch (Exception e) {
                log.error("Failed to upload photo: {}", e.getMessage());
                throw new RuntimeException("Failed to upload photo: " + file.getOriginalFilename(), e);
            }
        }

        return responses;
    }

    public List<IncidentPhotoResponse> getPhotosByIncident(String incidentId) {
        if (!incidentRepository.existsById(incidentId)) {
            throw new ResourceNotFoundException("Incident", "id", incidentId);
        }

        List<IncidentPhoto> photos = photoRepository.findByIncidentIdOrderByUploadedAtDesc(incidentId);

        return photos.stream()
                .map(this::toResponseWithFreshUrl)
                .collect(Collectors.toList());
    }

    public IncidentPhotoResponse getPhoto(String incidentId, String photoId) {
        IncidentPhoto photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo", "id", photoId));

        if (!photo.getIncident().getId().equals(incidentId)) {
            throw new ResourceNotFoundException("Photo", "id", photoId);
        }

        return toResponseWithFreshUrl(photo);
    }

    @Transactional
    public void deletePhoto(String incidentId, String photoId) {
        IncidentPhoto photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo", "id", photoId));

        if (!photo.getIncident().getId().equals(incidentId)) {
            throw new ResourceNotFoundException("Photo", "id", photoId);
        }

        User currentUser = userService.getCurrentUser();

        // Only uploader or admin can delete
        boolean isUploader = photo.getUploadedBy().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole().name().equals("ROLE_ADMIN");

        if (!isUploader && !isAdmin) {
            throw new ForbiddenException("You don't have permission to delete this photo");
        }

        try {
            minioService.deleteFile(photo.getFilePath());
        } catch (Exception e) {
            log.error("Failed to delete file from MinIO: {}", e.getMessage());
        }

        photoRepository.delete(photo);
        log.info("Photo {} deleted by {}", photoId, currentUser.getEmail());
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Invalid file type. Allowed types: JPEG, PNG, GIF, WebP");
        }
    }

    private IncidentPhotoResponse toResponse(IncidentPhoto photo) {
        return IncidentPhotoResponse.builder()
                .id(photo.getId())
                .fileName(photo.getFileName())
                .fileUrl(photo.getFileUrl())
                .fileSize(photo.getFileSize())
                .uploadedById(photo.getUploadedBy().getId())
                .uploadedByName(photo.getUploadedBy().getFullName())
                .uploadedAt(photo.getUploadedAt())
                .build();
    }

    private IncidentPhotoResponse toResponseWithFreshUrl(IncidentPhoto photo) {
        String freshUrl;
        try {
            freshUrl = minioService.getPresignedUrl(photo.getFilePath());
        } catch (Exception e) {
            log.error("Failed to generate presigned URL: {}", e.getMessage());
            freshUrl = photo.getFileUrl();
        }

        return IncidentPhotoResponse.builder()
                .id(photo.getId())
                .fileName(photo.getFileName())
                .fileUrl(freshUrl)
                .fileSize(photo.getFileSize())
                .uploadedById(photo.getUploadedBy().getId())
                .uploadedByName(photo.getUploadedBy().getFullName())
                .uploadedAt(photo.getUploadedAt())
                .build();
    }
}
