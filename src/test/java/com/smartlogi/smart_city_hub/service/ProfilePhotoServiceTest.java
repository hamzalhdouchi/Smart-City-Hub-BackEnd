package com.smartlogi.smart_city_hub.service;

import com.smartlogi.smart_city_hub.entity.ProfilePhoto;
import com.smartlogi.smart_city_hub.entity.User;
import com.smartlogi.smart_city_hub.entity.enums.Role;
import com.smartlogi.smart_city_hub.entity.enums.UserStatus;
import com.smartlogi.smart_city_hub.repository.ProfilePhotoRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProfilePhotoServiceTest {

    @Mock private ProfilePhotoRepository photoRepository;
    @Mock private MinioService minioService;
    @Mock private UserService userService;

    @InjectMocks
    private ProfilePhotoService profilePhotoService;

    private User mockUser;
    private MultipartFile validFile;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id("user-1")
                .email("user@example.com")
                .role(Role.ROLE_USER)
                .status(UserStatus.ACTIVE)
                .mustChangePassword(false)
                .build();

        validFile = mock(MultipartFile.class);
        when(validFile.isEmpty()).thenReturn(false);
        when(validFile.getSize()).thenReturn(1024L);
        when(validFile.getContentType()).thenReturn("image/jpeg");
        when(validFile.getOriginalFilename()).thenReturn("profile.jpg");
    }

    

    @Nested
    @DisplayName("uploadProfilePhoto")
    class UploadProfilePhotoTests {

        @Test
        void should_UploadPhoto_When_ValidFileProvided() throws Exception {
            when(userService.getCurrentUser()).thenReturn(mockUser);
            when(minioService.uploadFile(validFile, "users/user-1/profile"))
                    .thenReturn("users/user-1/profile/uuid-profile.jpg");
            when(minioService.getPresignedUrl("users/user-1/profile/uuid-profile.jpg"))
                    .thenReturn("http://minio/profile.jpg?token=xyz");
            when(photoRepository.save(any(ProfilePhoto.class))).thenAnswer(inv -> inv.getArgument(0));

            String resultUrl = profilePhotoService.uploadProfilePhoto(validFile);

            assertEquals("http://minio/profile.jpg?token=xyz", resultUrl);
            verify(photoRepository).save(any(ProfilePhoto.class));
        }

        @Test
        void should_ThrowIllegalArgument_When_FileIsEmpty() {
            MultipartFile emptyFile = mock(MultipartFile.class);
            when(emptyFile.isEmpty()).thenReturn(true);
            when(userService.getCurrentUser()).thenReturn(mockUser);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> profilePhotoService.uploadProfilePhoto(emptyFile));
            assertEquals("File is empty", ex.getMessage());
            verify(photoRepository, never()).save(any());
        }

        @Test
        void should_ThrowIllegalArgument_When_FileTooLarge() {
            MultipartFile largeFile = mock(MultipartFile.class);
            when(largeFile.isEmpty()).thenReturn(false);
            when(largeFile.getSize()).thenReturn(6 * 1024 * 1024L); 
            when(userService.getCurrentUser()).thenReturn(mockUser);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> profilePhotoService.uploadProfilePhoto(largeFile));
            assertTrue(ex.getMessage().contains("5MB"));
        }

        @Test
        void should_ThrowIllegalArgument_When_InvalidFileType() {
            MultipartFile pdfFile = mock(MultipartFile.class);
            when(pdfFile.isEmpty()).thenReturn(false);
            when(pdfFile.getSize()).thenReturn(1024L);
            when(pdfFile.getContentType()).thenReturn("application/pdf");
            when(userService.getCurrentUser()).thenReturn(mockUser);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> profilePhotoService.uploadProfilePhoto(pdfFile));
            assertTrue(ex.getMessage().contains("Invalid file type"));
        }

        @Test
        void should_AcceptPngFile() throws Exception {
            MultipartFile pngFile = mock(MultipartFile.class);
            when(pngFile.isEmpty()).thenReturn(false);
            when(pngFile.getSize()).thenReturn(500L);
            when(pngFile.getContentType()).thenReturn("image/png");
            when(pngFile.getOriginalFilename()).thenReturn("photo.png");

            when(userService.getCurrentUser()).thenReturn(mockUser);
            when(minioService.uploadFile(pngFile, "users/user-1/profile")).thenReturn("obj-name");
            when(minioService.getPresignedUrl("obj-name")).thenReturn("http://url");
            when(photoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            String result = profilePhotoService.uploadProfilePhoto(pngFile);

            assertEquals("http://url", result);
        }

        @Test
        void should_ThrowRuntimeException_When_MinioFails() throws Exception {
            when(userService.getCurrentUser()).thenReturn(mockUser);
            when(minioService.uploadFile(eq(validFile), anyString()))
                    .thenThrow(new RuntimeException("MinIO unavailable"));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> profilePhotoService.uploadProfilePhoto(validFile));
            assertEquals("Failed to upload profile photo", ex.getMessage());
        }
    }

    

    @Nested
    @DisplayName("getCurrentProfilePhotoUrl")
    class GetCurrentProfilePhotoUrlTests {

        @Test
        void should_ReturnFreshPresignedUrl_When_PhotoExists() throws Exception {
            ProfilePhoto photo = ProfilePhoto.builder()
                    .id("photo-1")
                    .user(mockUser)
                    .fileName("profile.jpg")
                    .filePath("users/user-1/profile/uuid.jpg")
                    .fileUrl("http://old-url")
                    .fileSize(1024L)
                    .build();

            when(photoRepository.findTopByUserIdOrderByUploadedAtDesc("user-1"))
                    .thenReturn(Optional.of(photo));
            when(minioService.getPresignedUrl("users/user-1/profile/uuid.jpg"))
                    .thenReturn("http://fresh-url");

            Optional<String> result = profilePhotoService.getCurrentProfilePhotoUrl("user-1");

            assertTrue(result.isPresent());
            assertEquals("http://fresh-url", result.get());
        }

        @Test
        void should_ReturnEmpty_When_NoPhotoExists() {
            when(photoRepository.findTopByUserIdOrderByUploadedAtDesc("user-1"))
                    .thenReturn(Optional.empty());

            Optional<String> result = profilePhotoService.getCurrentProfilePhotoUrl("user-1");

            assertFalse(result.isPresent());
        }

        @Test
        void should_FallbackToStoredUrl_When_PresignedUrlFails() throws Exception {
            ProfilePhoto photo = ProfilePhoto.builder()
                    .id("photo-1")
                    .user(mockUser)
                    .filePath("users/user-1/profile/uuid.jpg")
                    .fileUrl("http://stored-fallback-url")
                    .build();

            when(photoRepository.findTopByUserIdOrderByUploadedAtDesc("user-1"))
                    .thenReturn(Optional.of(photo));
            when(minioService.getPresignedUrl("users/user-1/profile/uuid.jpg"))
                    .thenThrow(new RuntimeException("MinIO error"));

            Optional<String> result = profilePhotoService.getCurrentProfilePhotoUrl("user-1");

            assertTrue(result.isPresent());
            assertEquals("http://stored-fallback-url", result.get());
        }
    }
}
