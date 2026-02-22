package com.smartlogi.smart_city_hub.repository;

import com.smartlogi.smart_city_hub.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, String> {

    Optional<Rating> findByIncidentId(String incidentId);

    boolean existsByIncidentId(String incidentId);

    @Query("SELECT AVG(r.stars) FROM Rating r")
    Double getAverageRating();

    @Query("SELECT AVG(r.stars) FROM Rating r WHERE r.incident.assignedAgent.id = :agentId")
    Double getAverageRatingByAgent(String agentId);
}
