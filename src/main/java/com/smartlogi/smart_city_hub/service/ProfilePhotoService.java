package com.smartlogi.smart_city_hub.service;

import com.smartlogi.smart_city_hub.entity.ProfilePhoto;
import com.smartlogi.smart_city_hub.entity.User;
import com.smartlogi.smart_city_hub.repository.ProfilePhotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfilePhotoService {

    private final ProfilePhotoRepository photoRepository;
    private final MinioService minioService;
    private final UserService userService;

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp");

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; 

    @Transactional
    public String uploadProfilePhoto(MultipartFile file) {
        User currentUser = userService.getCurrentUser();
        validateFile(file);

        try {
            
            String folder = "users/" + currentUser.getId() + "/profile";
            String objectName = minioService.uploadFile(file, folder);
            String fileUrl = minioService.getPresignedUrl(objectName);

            
            ProfilePhoto photo = ProfilePhoto.builder()
                    .user(currentUser)
                    .fileName(file.getOriginalFilename())
                    .filePath(objectName)
                    .fileUrl(fileUrl)
                    .fileSize(file.getSize())
                    .build();

            photoRepository.save(photo);
            log.info("Profile photo uploaded for user {}: {}", currentUser.getId(), objectName);

            return fileUrl;
        } catch (Exception e) {
            log.error("Failed to upload profile photo: {}", e.getMessage());
            throw new RuntimeException("Failed to upload profile photo", e);
        }
    }

    public Optional<String> getCurrentProfilePhotoUrl(String userId) {
        return photoRepository.findTopByUserIdOrderByUploadedAtDesc(userId)
                .map(photo -> {
                    try {
                        return minioService.getPresignedUrl(photo.getFilePath());
                    } catch (Exception e) {
                        log.error("Failed to refresh presigned URL: {}", e.getMessage());
                        return photo.getFileUrl();
                    }
                });
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Invalid file type. Allowed types: JPEG, PNG, GIF, WebP");
        }
    }
}
