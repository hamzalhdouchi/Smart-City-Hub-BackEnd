package com.smartlogi.smart_city_hub.service;

import com.smartlogi.smart_city_hub.dto.request.AssignAgentRequest;
import com.smartlogi.smart_city_hub.dto.request.CreateIncidentRequest;
import com.smartlogi.smart_city_hub.dto.request.UpdateStatusRequest;
import com.smartlogi.smart_city_hub.dto.response.IncidentResponse;
import com.smartlogi.smart_city_hub.entity.Category;
import com.smartlogi.smart_city_hub.entity.Incident;
import com.smartlogi.smart_city_hub.entity.IncidentPhoto;
import com.smartlogi.smart_city_hub.entity.User;
import com.smartlogi.smart_city_hub.entity.enums.IncidentStatus;
import com.smartlogi.smart_city_hub.entity.enums.Priority;
import com.smartlogi.smart_city_hub.entity.enums.Role;
import com.smartlogi.smart_city_hub.exception.BadRequestException;
import com.smartlogi.smart_city_hub.exception.ForbiddenException;
import com.smartlogi.smart_city_hub.exception.ResourceNotFoundException;
import com.smartlogi.smart_city_hub.mapper.IncidentMapper;
import com.smartlogi.smart_city_hub.repository.CategoryRepository;
import com.smartlogi.smart_city_hub.repository.IncidentPhotoRepository;
import com.smartlogi.smart_city_hub.repository.IncidentRepository;
import com.smartlogi.smart_city_hub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {

    @Mock
    private IncidentRepository incidentRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private IncidentPhotoRepository photoRepository;
    @Mock
    private UserService userService;
    @Mock
    private MinioService minioService;
    @Mock
    private IncidentMapper incidentMapper;

    @InjectMocks
    private IncidentService incidentService;

    private User mockUser;
    private User mockAgent;
    private Category mockCategory;
    private Incident mockIncident;
    private IncidentResponse mockIncidentResponse;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id("1")
                .email("user@example.com")
                .role(Role.ROLE_USER)
                .build();

        mockAgent = User.builder()
                .id("2")
                .email("agent@example.com")
                .role(Role.ROLE_AGENT)
                .build();

        mockCategory = Category.builder()
                .id("cat-1")
                .name("Test Category")
                .build();

        mockIncident = Incident.builder()
                .id("inc-1")
                .title("Test Incident")
                .description("Description")
                .status(IncidentStatus.NEW)
                .reporter(mockUser)
                .category(mockCategory)
                .build();

        mockIncidentResponse = new IncidentResponse();
        mockIncidentResponse.setId("inc-1");
        mockIncidentResponse.setTitle("Test Incident");
    }

    @Nested
    @DisplayName("createIncident")
    class CreateIncidentTests {

        @Test
        void should_CreateIncident_When_ValidRequest() {
            
            CreateIncidentRequest request = new CreateIncidentRequest();
            request.setCategoryId("cat-1");
            request.setTitle("Test Incident");
            request.setDescription("Description");

            when(userService.getCurrentUser()).thenReturn(mockUser);
            when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(mockCategory));
            when(incidentRepository.save(any(Incident.class))).thenReturn(mockIncident);
            when(incidentMapper.toResponse(any(Incident.class))).thenReturn(mockIncidentResponse);

            
            IncidentResponse response = incidentService.createIncident(request, null);

            
            assertNotNull(response);
            assertEquals("inc-1", response.getId());
            verify(incidentRepository, times(1)).save(any(Incident.class));
            verify(photoRepository, never()).save(any(IncidentPhoto.class));
        }

        @Test
        void should_ThrowException_When_CategoryNotFound() {
            
            CreateIncidentRequest request = new CreateIncidentRequest();
            request.setCategoryId("invalid-cat");

            when(userService.getCurrentUser()).thenReturn(mockUser);
            when(categoryRepository.findById("invalid-cat")).thenReturn(Optional.empty());

            
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> incidentService.createIncident(request, null));

            assertTrue(exception.getMessage().contains("Category"));
            verify(incidentRepository, never()).save(any(Incident.class));
        }

        @Test
        void should_UploadPhotos_When_PhotosProvided() throws Exception {
            
            CreateIncidentRequest request = new CreateIncidentRequest();
            request.setCategoryId("cat-1");
            MultipartFile mockPhoto = mock(MultipartFile.class);
            when(mockPhoto.isEmpty()).thenReturn(false);
            when(mockPhoto.getOriginalFilename()).thenReturn("test.jpg");
            when(mockPhoto.getSize()).thenReturn(1024L);

            when(userService.getCurrentUser()).thenReturn(mockUser);
            when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(mockCategory));
            when(incidentRepository.save(any(Incident.class))).thenReturn(mockIncident);
            when(minioService.uploadFile(any(), anyString())).thenReturn("incidents/inc-1/test.jpg");
            when(minioService.getPresignedUrl(anyString())).thenReturn("http://minio/test.jpg");
            when(incidentMapper.toResponse(any(Incident.class))).thenReturn(mockIncidentResponse);

            
            incidentService.createIncident(request, List.of(mockPhoto));

            
            verify(minioService).uploadFile(eq(mockPhoto), anyString());
            verify(photoRepository).save(any(IncidentPhoto.class));
        }

        @Test
        void should_HandleUploadFailure_When_MinioFails() throws Exception {
            
            CreateIncidentRequest request = new CreateIncidentRequest();
            request.setCategoryId("cat-1");
            MultipartFile mockPhoto = mock(MultipartFile.class);
            when(mockPhoto.isEmpty()).thenReturn(false);

            when(userService.getCurrentUser()).thenReturn(mockUser);
            when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(mockCategory));
            when(incidentRepository.save(any(Incident.class))).thenReturn(mockIncident);
            when(minioService.uploadFile(any(), anyString())).thenThrow(new RuntimeException("Upload failed"));
            when(incidentMapper.toResponse(any(Incident.class))).thenReturn(mockIncidentResponse);

            
            IncidentResponse response = incidentService.createIncident(request, List.of(mockPhoto));

            
            assertNotNull(response);
            verify(minioService).uploadFile(any(), anyString());
            verify(photoRepository, never()).save(any(IncidentPhoto.class));
        }
    }

    @Nested
    @DisplayName("getIncidentById")
    class GetIncidentByIdTests {

        @Test
        void should_ReturnIncident_When_Exists() {
            
            when(incidentRepository.findByIdWithRelations("inc-1")).thenReturn(Optional.of(mockIncident));
            when(incidentMapper.toResponse(mockIncident)).thenReturn(mockIncidentResponse);

            
            IncidentResponse response = incidentService.getIncidentById("inc-1");

            
            assertNotNull(response);
            assertEquals("inc-1", response.getId());
        }

        @Test
        void should_ThrowException_When_NotFound() {
            
            when(incidentRepository.findByIdWithRelations("invalid")).thenReturn(Optional.empty());

            
            assertThrows(ResourceNotFoundException.class, () -> incidentService.getIncidentById("invalid"));
        }
    }

    @Nested
    @DisplayName("Listing Incidents")
    class ListIncidentsTests {

        @Test
        void should_GetAllIncidents_WithFilters() {
            
            Page<Incident> page = new PageImpl<>(Collections.singletonList(mockIncident));
            when(incidentRepository.findWithFilters(any(), any(), any(), any())).thenReturn(page);
            when(incidentMapper.toResponse(any(Incident.class))).thenReturn(mockIncidentResponse);

            
            Page<IncidentResponse> result = incidentService.getAllIncidents(null, null, Pageable.unpaged());

            
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
        }

        @Test
        void should_GetMyIncidents() {
            
            when(userService.getCurrentUser()).thenReturn(mockUser);
            Page<Incident> page = new PageImpl<>(Collections.singletonList(mockIncident));
            when(incidentRepository.findByReporterId(eq("1"), any())).thenReturn(page);
            when(incidentMapper.toResponse(any(Incident.class))).thenReturn(mockIncidentResponse);

            
            Page<IncidentResponse> result = incidentService.getMyIncidents(Pageable.unpaged());

            
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
        }

        @Test
        void should_GetAssignedIncidents() {
            
            when(userService.getCurrentUser()).thenReturn(mockAgent);
            Page<Incident> page = new PageImpl<>(Collections.singletonList(mockIncident));
            when(incidentRepository.findByAssignedAgentId(eq("2"), any())).thenReturn(page);
            when(incidentMapper.toResponse(any(Incident.class))).thenReturn(mockIncidentResponse);

            
            Page<IncidentResponse> result = incidentService.getAssignedIncidents(Pageable.unpaged());

            
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
        }
    }

    @Nested
    @DisplayName("updateStatus")
    class UpdateStatusTests {

        @Test
        void should_UpdateStatus_When_TransitionIsValid() {
            
            UpdateStatusRequest request = new UpdateStatusRequest();
            request.setStatus(IncidentStatus.ASSIGNED);

            
            
            
            
            
            
            
            

            
            
            
            
            
            
            
            

            when(userService.getCurrentUser()).thenReturn(mockUser);
            when(incidentRepository.findById("inc-1")).thenReturn(Optional.of(mockIncident));
            when(incidentRepository.save(any(Incident.class))).thenReturn(mockIncident);
            when(incidentMapper.toResponse(any(Incident.class))).thenReturn(mockIncidentResponse);

            
            IncidentResponse response = incidentService.updateStatus("inc-1", request);

            
            verify(incidentRepository).save(mockIncident);
            assertEquals(IncidentStatus.ASSIGNED, mockIncident.getStatus());
        }

        @Test
        void should_ThrowForbidden_When_AgentUpdatesUnassignedIncident() {
            
            UpdateStatusRequest request = new UpdateStatusRequest();
            request.setStatus(IncidentStatus.IN_PROGRESS); 

            mockIncident.setStatus(IncidentStatus.ASSIGNED);
            

            when(userService.getCurrentUser()).thenReturn(mockAgent); 
            when(incidentRepository.findById("inc-1")).thenReturn(Optional.of(mockIncident));

            
            assertThrows(ForbiddenException.class, () -> incidentService.updateStatus("inc-1", request));
        }

        @Test
        void should_ThrowBadRequest_When_TransitionInvalid() {
            
            
            UpdateStatusRequest request = new UpdateStatusRequest();
            request.setStatus(IncidentStatus.RESOLVED);

            when(userService.getCurrentUser()).thenReturn(mockUser);
            when(incidentRepository.findById("inc-1")).thenReturn(Optional.of(mockIncident));

            
            assertThrows(BadRequestException.class, () -> incidentService.updateStatus("inc-1", request));
        }

        @Test
        void should_SetResolvedAt_When_StatusIsResolved() {
            
            mockIncident.setStatus(IncidentStatus.IN_PROGRESS);
            UpdateStatusRequest request = new UpdateStatusRequest();
            request.setStatus(IncidentStatus.RESOLVED);

            when(userService.getCurrentUser()).thenReturn(mockUser);
            when(incidentRepository.findById("inc-1")).thenReturn(Optional.of(mockIncident));
            when(incidentRepository.save(any(Incident.class))).thenReturn(mockIncident);
            when(incidentMapper.toResponse(any(Incident.class))).thenReturn(mockIncidentResponse);

            
            incidentService.updateStatus("inc-1", request);

            
            assertNotNull(mockIncident.getResolvedAt());
        }
    }

    @Nested
    @DisplayName("assignAgent")
    class AssignAgentTests {

        @Test
        void should_AssignAgent_When_Valid() {
            
            AssignAgentRequest request = new AssignAgentRequest();
            request.setAgentId("2");

            when(userService.getCurrentUser()).thenReturn(mockUser);
            when(incidentRepository.findById("inc-1")).thenReturn(Optional.of(mockIncident));
            when(userRepository.findById("2")).thenReturn(Optional.of(mockAgent));
            when(incidentRepository.save(any(Incident.class))).thenReturn(mockIncident);
            when(incidentMapper.toResponse(any(Incident.class))).thenReturn(mockIncidentResponse);

            
            IncidentResponse response = incidentService.assignAgent("inc-1", request);

            
            verify(incidentRepository).save(mockIncident);
            assertEquals(mockAgent, mockIncident.getAssignedAgent());
            assertEquals(IncidentStatus.ASSIGNED, mockIncident.getStatus());
        }

        @Test
        void should_ThrowBadRequest_When_UserNotAgent() {
            
            AssignAgentRequest request = new AssignAgentRequest();
            request.setAgentId("1"); 

            when(incidentRepository.findById("inc-1")).thenReturn(Optional.of(mockIncident));
            when(userRepository.findById("1")).thenReturn(Optional.of(mockUser));

            
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> incidentService.assignAgent("inc-1", request));
            assertEquals("User is not an agent", exception.getMessage());
        }
    }
}
