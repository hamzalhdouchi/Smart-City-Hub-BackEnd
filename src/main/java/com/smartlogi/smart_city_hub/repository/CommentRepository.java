package com.smartlogi.smart_city_hub.repository;

import com.smartlogi.smart_city_hub.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    List<Comment> findByIncidentIdOrderByCreatedAtDesc(Long incidentId);
    
    Page<Comment> findByIncidentId(Long incidentId, Pageable pageable);
    
    void deleteByIncidentId(Long incidentId);
}
