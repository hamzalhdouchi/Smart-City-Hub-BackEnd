package com.smartlogi.smart_city_hub.service;

import com.smartlogi.smart_city_hub.dto.response.StatisticsResponse;
import com.smartlogi.smart_city_hub.entity.enums.Role;
import com.smartlogi.smart_city_hub.repository.IncidentRepository;
import com.smartlogi.smart_city_hub.repository.RatingRepository;
import com.smartlogi.smart_city_hub.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock private IncidentRepository incidentRepository;
    @Mock private UserRepository userRepository;
    @Mock private RatingRepository ratingRepository;

    @InjectMocks
    private StatisticsService statisticsService;

    private static List<Object[]> statusRows(Object[]... rows) {
        List<Object[]> list = new ArrayList<>();
        for (Object[] row : rows) {
            list.add(row);
        }
        return list;
    }

    

    @Nested
    @DisplayName("getGlobalStatistics")
    class GetGlobalStatisticsTests {

        @Test
        void should_ReturnCompleteStatistics() {
            when(incidentRepository.count()).thenReturn(42L);
            when(userRepository.count()).thenReturn(20L);
            when(userRepository.findByRole(Role.ROLE_AGENT)).thenReturn(List.of());
            when(incidentRepository.countByStatusGrouped())
                    .thenReturn(statusRows(new Object[]{"NEW", 10L}, new Object[]{"RESOLVED", 5L}));
            when(incidentRepository.countByCategoryGrouped())
                    .thenReturn(statusRows(new Object[]{"Infrastructure", 15L}));
            when(ratingRepository.getAverageRating()).thenReturn(4.0);

            StatisticsResponse result = statisticsService.getGlobalStatistics();

            assertNotNull(result);
            assertEquals(42L, result.getTotalIncidents());
            assertEquals(20L, result.getTotalUsers());
            assertEquals(0L, result.getTotalAgents());
            assertEquals(4.0, result.getAverageRating());
            assertTrue(result.getIncidentsByStatus().containsKey("NEW"));
            assertEquals(10L, result.getIncidentsByStatus().get("NEW"));
            assertTrue(result.getIncidentsByCategory().containsKey("Infrastructure"));
        }

        @Test
        void should_ReturnZeroAverageRating_When_NoRatings() {
            when(incidentRepository.count()).thenReturn(5L);
            when(userRepository.count()).thenReturn(3L);
            when(userRepository.findByRole(Role.ROLE_AGENT)).thenReturn(List.of());
            when(incidentRepository.countByStatusGrouped()).thenReturn(new ArrayList<>());
            when(incidentRepository.countByCategoryGrouped()).thenReturn(new ArrayList<>());
            when(ratingRepository.getAverageRating()).thenReturn(null);

            StatisticsResponse result = statisticsService.getGlobalStatistics();

            assertEquals(0.0, result.getAverageRating());
        }

        @Test
        void should_CountAgentsCorrectly() {
            com.smartlogi.smart_city_hub.entity.User agent1 = mock(com.smartlogi.smart_city_hub.entity.User.class);
            com.smartlogi.smart_city_hub.entity.User agent2 = mock(com.smartlogi.smart_city_hub.entity.User.class);

            when(incidentRepository.count()).thenReturn(0L);
            when(userRepository.count()).thenReturn(5L);
            when(userRepository.findByRole(Role.ROLE_AGENT)).thenReturn(List.of(agent1, agent2));
            when(incidentRepository.countByStatusGrouped()).thenReturn(new ArrayList<>());
            when(incidentRepository.countByCategoryGrouped()).thenReturn(new ArrayList<>());
            when(ratingRepository.getAverageRating()).thenReturn(null);

            StatisticsResponse result = statisticsService.getGlobalStatistics();

            assertEquals(2L, result.getTotalAgents());
        }
    }

    

    @Nested
    @DisplayName("getIncidentsByStatus")
    class GetIncidentsByStatusTests {

        @Test
        void should_ReturnMapOfStatusCounts() {
            when(incidentRepository.countByStatusGrouped()).thenReturn(statusRows(
                    new Object[]{"NEW", 5L},
                    new Object[]{"ASSIGNED", 3L},
                    new Object[]{"RESOLVED", 8L}
            ));

            Map<String, Long> result = statisticsService.getIncidentsByStatus();

            assertEquals(3, result.size());
            assertEquals(5L, result.get("NEW"));
            assertEquals(3L, result.get("ASSIGNED"));
            assertEquals(8L, result.get("RESOLVED"));
        }

        @Test
        void should_ReturnEmptyMap_When_NoIncidents() {
            when(incidentRepository.countByStatusGrouped()).thenReturn(new ArrayList<>());

            Map<String, Long> result = statisticsService.getIncidentsByStatus();

            assertTrue(result.isEmpty());
        }
    }

    

    @Nested
    @DisplayName("getIncidentsByCategory")
    class GetIncidentsByCategoryTests {

        @Test
        void should_ReturnMapOfCategoryCounts() {
            when(incidentRepository.countByCategoryGrouped()).thenReturn(statusRows(
                    new Object[]{"Infrastructure", 12L},
                    new Object[]{"Environment", 7L}
            ));

            Map<String, Long> result = statisticsService.getIncidentsByCategory();

            assertEquals(2, result.size());
            assertEquals(12L, result.get("Infrastructure"));
            assertEquals(7L, result.get("Environment"));
        }

        @Test
        void should_ReturnEmptyMap_When_NoIncidents() {
            when(incidentRepository.countByCategoryGrouped()).thenReturn(new ArrayList<>());

            Map<String, Long> result = statisticsService.getIncidentsByCategory();

            assertTrue(result.isEmpty());
        }
    }
}
