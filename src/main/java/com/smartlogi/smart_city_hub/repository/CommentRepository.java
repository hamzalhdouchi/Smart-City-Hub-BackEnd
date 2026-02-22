package com.smartlogi.smart_city_hub.repository;

import com.smartlogi.smart_city_hub.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {

    List<Comment> findByIncidentIdOrderByCreatedAtDesc(String incidentId);

    Page<Comment> findByIncidentId(String incidentId, Pageable pageable);

    void deleteByIncidentId(String incidentId);
}
