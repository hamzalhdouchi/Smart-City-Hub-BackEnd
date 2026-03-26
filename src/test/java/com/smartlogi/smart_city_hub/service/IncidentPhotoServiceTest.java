package com.smartlogi.smart_city_hub.service;

import com.smartlogi.smart_city_hub.dto.response.IncidentPhotoResponse;
import com.smartlogi.smart_city_hub.entity.Incident;
import com.smartlogi.smart_city_hub.entity.IncidentPhoto;
import com.smartlogi.smart_city_hub.entity.User;
import com.smartlogi.smart_city_hub.entity.enums.IncidentStatus;
import com.smartlogi.smart_city_hub.entity.enums.Role;
import com.smartlogi.smart_city_hub.entity.enums.UserStatus;
import com.smartlogi.smart_city_hub.exception.ForbiddenException;
import com.smartlogi.smart_city_hub.exception.ResourceNotFoundException;
import com.smartlogi.smart_city_hub.repository.IncidentPhotoRepository;
import com.smartlogi.smart_city_hub.repository.IncidentRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IncidentPhotoServiceTest {

    @Mock private IncidentPhotoRepository photoRepository;
    @Mock private IncidentRepository incidentRepository;
    @Mock private MinioService minioService;
    @Mock private UserService userService;

    @InjectMocks
    private IncidentPhotoService incidentPhotoService;

    private User mockUser;
    private User mockAdmin;
    private User mockOtherUser;
    private Incident mockIncident;
    private IncidentPhoto mockPhoto;
    private MultipartFile validFile;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id("user-1")
                .email("user@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(Role.ROLE_USER)
                .status(UserStatus.ACTIVE)
                .mustChangePassword(false)
                .build();

        mockAdmin = User.builder()
                .id("admin-1")
                .email("admin@example.com")
                .firstName("Admin")
                .lastName("User")
                .role(Role.ROLE_ADMIN)
                .status(UserStatus.ACTIVE)
                .mustChangePassword(false)
                .build();

        mockOtherUser = User.builder()
                .id("other-1")
                .email("other@example.com")
                .firstName("Other")
                .lastName("User")
                .role(Role.ROLE_USER)
                .status(UserStatus.ACTIVE)
                .mustChangePassword(false)
                .build();

        mockIncident = Incident.builder()
                .id("inc-1")
                .title("Test Incident")
                .status(IncidentStatus.NEW)
                .reporter(mockUser)
                .build();

        mockPhoto = IncidentPhoto.builder()
                .id("photo-1")
                .incident(mockIncident)
                .uploadedBy(mockUser)
                .fileName("test.jpg")
                .filePath("incidents/inc-1/uuid.jpg")
                .fileUrl("http://minio/incidents/inc-1/uuid.jpg")
                .fileSize(1024L)
                .build();

        validFile = mock(MultipartFile.class);
        when(validFile.isEmpty()).thenReturn(false);
        when(validFile.getSize()).thenReturn(1024L);
        when(validFile.getContentType()).thenReturn("image/jpeg");
        when(validFile.getOriginalFilename()).thenReturn("photo.jpg");
    }

    

    @Nested
    @DisplayName("uploadPhotos")
    class UploadPhotosTests {

        @Test
        void should_UploadPhotos_When_ValidFilesProvided() throws Exception {
            when(incidentRepository.findById("inc-1")).thenReturn(Optional.of(mockIncident));
            when(userService.getCurrentUser()).thenReturn(mockUser);
            when(minioService.uploadFile(validFile, "incidents/inc-1")).thenReturn("incidents/inc-1/uuid.jpg");
            when(minioService.getPresignedUrl("incidents/inc-1/uuid.jpg")).thenReturn("http://minio/url");
            when(photoRepository.save(any(IncidentPhoto.class))).thenReturn(mockPhoto);

            List<IncidentPhotoResponse> result = incidentPhotoService.uploadPhotos("inc-1", List.of(validFile));

            assertEquals(1, result.size());
            verify(photoRepository).save(any(IncidentPhoto.class));
        }

        @Test
        void should_ThrowNotFound_When_IncidentDoesNotExist() {
            when(incidentRepository.findById("nonexistent")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> incidentPhotoService.uploadPhotos("nonexistent", List.of(validFile)));
            verify(photoRepository, never()).save(any());
        }

        @Test
        void should_ThrowIllegalArgument_When_FileIsEmpty() {
            MultipartFile emptyFile = mock(MultipartFile.class);
            when(emptyFile.isEmpty()).thenReturn(true);

            when(incidentRepository.findById("inc-1")).thenReturn(Optional.of(mockIncident));
            when(userService.getCurrentUser()).thenReturn(mockUser);

            assertThrows(IllegalArgumentException.class,
                    () -> incidentPhotoService.uploadPhotos("inc-1", List.of(emptyFile)));
        }

        @Test
        void should_ThrowIllegalArgument_When_FileTooLarge() {
            MultipartFile largeFile = mock(MultipartFile.class);
            when(largeFile.isEmpty()).thenReturn(false);
            when(largeFile.getSize()).thenReturn(11 * 1024 * 1024L); 

            when(incidentRepository.findById("inc-1")).thenReturn(Optional.of(mockIncident));
            when(userService.getCurrentUser()).thenReturn(mockUser);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> incidentPhotoService.uploadPhotos("inc-1", List.of(largeFile)));
            assertTrue(ex.getMessage().contains("10MB"));
        }

        @Test
        void should_ThrowIllegalArgument_When_InvalidContentType() {
            MultipartFile invalidFile = mock(MultipartFile.class);
            when(invalidFile.isEmpty()).thenReturn(false);
            when(invalidFile.getSize()).thenReturn(1024L);
            when(invalidFile.getContentType()).thenReturn("application/pdf");

            when(incidentRepository.findById("inc-1")).thenReturn(Optional.of(mockIncident));
            when(userService.getCurrentUser()).thenReturn(mockUser);

            assertThrows(IllegalArgumentException.class,
                    () -> incidentPhotoService.uploadPhotos("inc-1", List.of(invalidFile)));
        }

        @Test
        void should_ThrowRuntimeException_When_MinioFails() throws Exception {
            when(incidentRepository.findById("inc-1")).thenReturn(Optional.of(mockIncident));
            when(userService.getCurrentUser()).thenReturn(mockUser);
            when(minioService.uploadFile(eq(validFile), anyString()))
                    .thenThrow(new RuntimeException("Storage error"));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> incidentPhotoService.uploadPhotos("inc-1", List.of(validFile)));
            assertTrue(ex.getMessage().contains("Failed to upload photo"));
        }
    }

    

    @Nested
    @DisplayName("getPhotosByIncident")
    class GetPhotosByIncidentTests {

        @Test
        void should_ReturnPhotos_When_IncidentExists() throws Exception {
            when(incidentRepository.existsById("inc-1")).thenReturn(true);
            when(photoRepository.findByIncidentIdOrderByUploadedAtDesc("inc-1"))
                    .thenReturn(List.of(mockPhoto));
            when(minioService.getPresignedUrl("incidents/inc-1/uuid.jpg"))
                    .thenReturn("http://fresh-url");

            List<IncidentPhotoResponse> result = incidentPhotoService.getPhotosByIncident("inc-1");

            assertEquals(1, result.size());
            assertEquals("photo-1", result.get(0).getId());
        }

        @Test
        void should_ThrowNotFound_When_IncidentDoesNotExist() {
            when(incidentRepository.existsById("nonexistent")).thenReturn(false);

            assertThrows(ResourceNotFoundException.class,
                    () -> incidentPhotoService.getPhotosByIncident("nonexistent"));
        }

        @Test
        void should_UseFallbackUrl_When_PresignedUrlFails() throws Exception {
            when(incidentRepository.existsById("inc-1")).thenReturn(true);
            when(photoRepository.findByIncidentIdOrderByUploadedAtDesc("inc-1"))
                    .thenReturn(List.of(mockPhoto));
            when(minioService.getPresignedUrl("incidents/inc-1/uuid.jpg"))
                    .thenThrow(new RuntimeException("MinIO error"));

            List<IncidentPhotoResponse> result = incidentPhotoService.getPhotosByIncident("inc-1");

            assertEquals(1, result.size());
            assertEquals("http://minio/incidents/inc-1/uuid.jpg", result.get(0).getFileUrl());
        }
    }

    

    @Nested
    @DisplayName("getPhoto")
    class GetPhotoTests {

        @Test
        void should_ReturnPhoto_When_Found() throws Exception {
            when(photoRepository.findById("photo-1")).thenReturn(Optional.of(mockPhoto));
            when(minioService.getPresignedUrl("incidents/inc-1/uuid.jpg")).thenReturn("http://fresh-url");

            IncidentPhotoResponse result = incidentPhotoService.getPhoto("inc-1", "photo-1");

            assertNotNull(result);
            assertEquals("photo-1", result.getId());
        }

        @Test
        void should_ThrowNotFound_When_PhotoDoesNotExist() {
            when(photoRepository.findById("nonexistent")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> incidentPhotoService.getPhoto("inc-1", "nonexistent"));
        }

        @Test
        void should_ThrowNotFound_When_PhotoBelongsToDifferentIncident() {
            Incident otherIncident = Incident.builder().id("inc-99").build();
            IncidentPhoto photoOnOtherIncident = IncidentPhoto.builder()
                    .id("photo-1")
                    .incident(otherIncident)
                    .uploadedBy(mockUser)
                    .filePath("incidents/inc-99/uuid.jpg")
                    .build();

            when(photoRepository.findById("photo-1")).thenReturn(Optional.of(photoOnOtherIncident));

            assertThrows(ResourceNotFoundException.class,
                    () -> incidentPhotoService.getPhoto("inc-1", "photo-1"));
        }
    }

    

    @Nested
    @DisplayName("deletePhoto")
    class DeletePhotoTests {

        @Test
        void should_DeletePhoto_When_Uploader() throws Exception {
            when(photoRepository.findById("photo-1")).thenReturn(Optional.of(mockPhoto));
            when(userService.getCurrentUser()).thenReturn(mockUser);

            assertDoesNotThrow(() -> incidentPhotoService.deletePhoto("inc-1", "photo-1"));

            verify(photoRepository).delete(mockPhoto);
        }

        @Test
        void should_DeletePhoto_When_Admin() throws Exception {
            when(photoRepository.findById("photo-1")).thenReturn(Optional.of(mockPhoto));
            when(userService.getCurrentUser()).thenReturn(mockAdmin);

            assertDoesNotThrow(() -> incidentPhotoService.deletePhoto("inc-1", "photo-1"));

            verify(photoRepository).delete(mockPhoto);
        }

        @Test
        void should_ThrowForbidden_When_NeitherUploaderNorAdmin() {
            when(photoRepository.findById("photo-1")).thenReturn(Optional.of(mockPhoto));
            when(userService.getCurrentUser()).thenReturn(mockOtherUser);

            ForbiddenException ex = assertThrows(ForbiddenException.class,
                    () -> incidentPhotoService.deletePhoto("inc-1", "photo-1"));
            assertEquals("You don't have permission to delete this photo", ex.getMessage());
            verify(photoRepository, never()).delete(any());
        }

        @Test
        void should_ThrowNotFound_When_PhotoDoesNotExist() {
            when(photoRepository.findById("nonexistent")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> incidentPhotoService.deletePhoto("inc-1", "nonexistent"));
        }

        @Test
        void should_ThrowNotFound_When_PhotoBelongsToDifferentIncident() {
            Incident otherIncident = Incident.builder().id("inc-99").build();
            IncidentPhoto photoOnOtherIncident = IncidentPhoto.builder()
                    .id("photo-1")
                    .incident(otherIncident)
                    .uploadedBy(mockUser)
                    .filePath("incidents/inc-99/uuid.jpg")
                    .build();

            when(photoRepository.findById("photo-1")).thenReturn(Optional.of(photoOnOtherIncident));

            assertThrows(ResourceNotFoundException.class,
                    () -> incidentPhotoService.deletePhoto("inc-1", "photo-1"));
        }

        @Test
        void should_StillDeleteFromDB_When_MinioDeleteFails() throws Exception {
            when(photoRepository.findById("photo-1")).thenReturn(Optional.of(mockPhoto));
            when(userService.getCurrentUser()).thenReturn(mockUser);
            doThrow(new RuntimeException("MinIO error"))
                    .when(minioService).deleteFile(anyString());

            assertDoesNotThrow(() -> incidentPhotoService.deletePhoto("inc-1", "photo-1"));
            verify(photoRepository).delete(mockPhoto);
        }
    }
}
