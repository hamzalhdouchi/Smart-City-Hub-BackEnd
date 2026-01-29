package com.smartlogi.smart_city_hub.service;

import com.smartlogi.smart_city_hub.dto.request.CreateRatingRequest;
import com.smartlogi.smart_city_hub.dto.response.RatingResponse;
import com.smartlogi.smart_city_hub.entity.Incident;
import com.smartlogi.smart_city_hub.entity.Rating;
import com.smartlogi.smart_city_hub.entity.User;
import com.smartlogi.smart_city_hub.entity.enums.IncidentStatus;
import com.smartlogi.smart_city_hub.exception.BadRequestException;
import com.smartlogi.smart_city_hub.exception.ForbiddenException;
import com.smartlogi.smart_city_hub.exception.ResourceNotFoundException;
import com.smartlogi.smart_city_hub.mapper.RatingMapper;
import com.smartlogi.smart_city_hub.repository.IncidentRepository;
import com.smartlogi.smart_city_hub.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final IncidentRepository incidentRepository;
    private final UserService userService;
    private final RatingMapper ratingMapper;

    public RatingResponse getRatingByIncident(Long incidentId) {
        Rating rating = ratingRepository.findByIncidentId(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found for this incident"));
        return ratingMapper.toResponse(rating);
    }

    @Transactional
    public RatingResponse rateIncident(Long incidentId, CreateRatingRequest request) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", incidentId));

        User currentUser = userService.getCurrentUser();

        // Only the reporter can rate their own incident
        if (!incident.getReporter().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only rate your own incidents");
        }

        if (incident.getStatus() != IncidentStatus.RESOLVED && incident.getStatus() != IncidentStatus.VALIDATED) {
            throw new BadRequestException("Can only rate resolved or validated incidents");
        }

        // Check if already rated
        if (ratingRepository.existsByIncidentId(incidentId)) {
            throw new BadRequestException("Incident already rated");
        }

        Rating rating = Rating.builder()
                .incident(incident)
                .user(currentUser)
                .stars(request.getStars())
                .feedback(request.getFeedback())
                .build();

        rating = ratingRepository.save(rating);
        log.info("Incident {} rated with {} stars by {}", incidentId, request.getStars(), currentUser.getEmail());

        return ratingMapper.toResponse(rating);
    }

    public Double getAverageRating() {
        return ratingRepository.getAverageRating();
    }

    public Double getAverageRatingByAgent(Long agentId) {
        return ratingRepository.getAverageRatingByAgent(agentId);
    }
}
