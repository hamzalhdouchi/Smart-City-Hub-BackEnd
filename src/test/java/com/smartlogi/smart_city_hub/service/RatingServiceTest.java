package com.smartlogi.smart_city_hub.service;

import com.smartlogi.smart_city_hub.dto.request.CreateRatingRequest;
import com.smartlogi.smart_city_hub.dto.response.RatingResponse;
import com.smartlogi.smart_city_hub.entity.Incident;
import com.smartlogi.smart_city_hub.entity.Rating;
import com.smartlogi.smart_city_hub.entity.User;
import com.smartlogi.smart_city_hub.entity.enums.IncidentStatus;
import com.smartlogi.smart_city_hub.entity.enums.Role;
import com.smartlogi.smart_city_hub.entity.enums.UserStatus;
import com.smartlogi.smart_city_hub.exception.BadRequestException;
import com.smartlogi.smart_city_hub.exception.ForbiddenException;
import com.smartlogi.smart_city_hub.exception.ResourceNotFoundException;
import com.smartlogi.smart_city_hub.mapper.RatingMapper;
import com.smartlogi.smart_city_hub.repository.IncidentRepository;
import com.smartlogi.smart_city_hub.repository.RatingRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock private RatingRepository ratingRepository;
    @Mock private IncidentRepository incidentRepository;
    @Mock private UserService userService;
    @Mock private RatingMapper ratingMapper;

    @InjectMocks
    private RatingService ratingService;

    private User reporter;
    private User otherUser;
    private Incident resolvedIncident;
    private Rating mockRating;
    private RatingResponse mockRatingResponse;

    @BeforeEach
    void setUp() {
        reporter = User.builder()
                .id("user-1")
                .email("reporter@example.com")
                .role(Role.ROLE_USER)
                .status(UserStatus.ACTIVE)
                .mustChangePassword(false)
                .build();

        otherUser = User.builder()
                .id("other-1")
                .email("other@example.com")
                .role(Role.ROLE_USER)
                .status(UserStatus.ACTIVE)
                .mustChangePassword(false)
                .build();

        resolvedIncident = Incident.builder()
                .id("inc-1")
                .title("Test Incident")
                .status(IncidentStatus.RESOLVED)
                .reporter(reporter)
                .build();

        mockRating = Rating.builder()
                .id("rating-1")
                .incident(resolvedIncident)
                .user(reporter)
                .stars(4)
                .feedback("Good service")
                .build();

        mockRatingResponse = new RatingResponse();
        mockRatingResponse.setId("rating-1");
        mockRatingResponse.setStars(4);
    }

    

    @Nested
    @DisplayName("getRatingByIncident")
    class GetRatingByIncidentTests {

        @Test
        void should_ReturnRating_When_Found() {
            when(ratingRepository.findByIncidentId("inc-1")).thenReturn(Optional.of(mockRating));
            when(ratingMapper.toResponse(mockRating)).thenReturn(mockRatingResponse);

            RatingResponse result = ratingService.getRatingByIncident("inc-1");

            assertNotNull(result);
            assertEquals("rating-1", result.getId());
            assertEquals(4, result.getStars());
        }

        @Test
        void should_ThrowNotFound_When_NoRatingForIncident() {
            when(ratingRepository.findByIncidentId("inc-2")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> ratingService.getRatingByIncident("inc-2"));
        }
    }

    

    @Nested
    @DisplayName("rateIncident")
    class RateIncidentTests {

        @Test
        void should_CreateRating_When_ValidRequest() {
            CreateRatingRequest request = new CreateRatingRequest();
            request.setStars(5);
            request.setFeedback("Excellent!");

            when(incidentRepository.findById("inc-1")).thenReturn(Optional.of(resolvedIncident));
            when(userService.getCurrentUser()).thenReturn(reporter);
            when(ratingRepository.existsByIncidentId("inc-1")).thenReturn(false);
            when(ratingRepository.save(any(Rating.class))).thenReturn(mockRating);
            when(ratingMapper.toResponse(mockRating)).thenReturn(mockRatingResponse);

            RatingResponse result = ratingService.rateIncident("inc-1", request);

            assertNotNull(result);
            verify(ratingRepository).save(any(Rating.class));
        }

        @Test
        void should_AllowRating_When_IncidentIsValidated() {
            resolvedIncident.setStatus(IncidentStatus.VALIDATED);
            CreateRatingRequest request = new CreateRatingRequest();
            request.setStars(3);

            when(incidentRepository.findById("inc-1")).thenReturn(Optional.of(resolvedIncident));
            when(userService.getCurrentUser()).thenReturn(reporter);
            when(ratingRepository.existsByIncidentId("inc-1")).thenReturn(false);
            when(ratingRepository.save(any(Rating.class))).thenReturn(mockRating);
            when(ratingMapper.toResponse(mockRating)).thenReturn(mockRatingResponse);

            RatingResponse result = ratingService.rateIncident("inc-1", request);

            assertNotNull(result);
        }

        @Test
        void should_ThrowNotFound_When_IncidentDoesNotExist() {
            CreateRatingRequest request = new CreateRatingRequest();
            when(incidentRepository.findById("nonexistent")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> ratingService.rateIncident("nonexistent", request));
            verify(ratingRepository, never()).save(any());
        }

        @Test
        void should_ThrowForbidden_When_NotReporter() {
            CreateRatingRequest request = new CreateRatingRequest();
            request.setStars(4);

            when(incidentRepository.findById("inc-1")).thenReturn(Optional.of(resolvedIncident));
            when(userService.getCurrentUser()).thenReturn(otherUser);

            ForbiddenException ex = assertThrows(ForbiddenException.class,
                    () -> ratingService.rateIncident("inc-1", request));
            assertEquals("You can only rate your own incidents", ex.getMessage());
        }

        @Test
        void should_ThrowBadRequest_When_IncidentNotResolved() {
            resolvedIncident.setStatus(IncidentStatus.IN_PROGRESS);
            CreateRatingRequest request = new CreateRatingRequest();
            request.setStars(4);

            when(incidentRepository.findById("inc-1")).thenReturn(Optional.of(resolvedIncident));
            when(userService.getCurrentUser()).thenReturn(reporter);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> ratingService.rateIncident("inc-1", request));
            assertEquals("Can only rate resolved or validated incidents", ex.getMessage());
        }

        @Test
        void should_ThrowBadRequest_When_AlreadyRated() {
            CreateRatingRequest request = new CreateRatingRequest();
            request.setStars(4);

            when(incidentRepository.findById("inc-1")).thenReturn(Optional.of(resolvedIncident));
            when(userService.getCurrentUser()).thenReturn(reporter);
            when(ratingRepository.existsByIncidentId("inc-1")).thenReturn(true);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> ratingService.rateIncident("inc-1", request));
            assertEquals("Incident already rated", ex.getMessage());
        }
    }

    

    @Nested
    @DisplayName("getAverageRating")
    class GetAverageRatingTests {

        @Test
        void should_ReturnAverageRating() {
            when(ratingRepository.getAverageRating()).thenReturn(4.2);

            Double result = ratingService.getAverageRating();

            assertEquals(4.2, result);
        }

        @Test
        void should_ReturnNull_When_NoRatingsExist() {
            when(ratingRepository.getAverageRating()).thenReturn(null);

            Double result = ratingService.getAverageRating();

            assertNull(result);
        }
    }

    

    @Nested
    @DisplayName("getAverageRatingByAgent")
    class GetAverageRatingByAgentTests {

        @Test
        void should_ReturnAgentAverageRating() {
            when(ratingRepository.getAverageRatingByAgent("agent-1")).thenReturn(3.8);

            Double result = ratingService.getAverageRatingByAgent("agent-1");

            assertEquals(3.8, result);
        }

        @Test
        void should_ReturnNull_When_AgentHasNoRatings() {
            when(ratingRepository.getAverageRatingByAgent("agent-1")).thenReturn(null);

            Double result = ratingService.getAverageRatingByAgent("agent-1");

            assertNull(result);
        }
    }
}
