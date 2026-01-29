package com.smartlogi.smart_city_hub.repository;

import com.smartlogi.smart_city_hub.entity.Incident;
import com.smartlogi.smart_city_hub.entity.enums.IncidentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    
    Page<Incident> findByReporterId(Long reporterId, Pageable pageable);
    
    Page<Incident> findByAssignedAgentId(Long agentId, Pageable pageable);
    
    Page<Incident> findByStatus(IncidentStatus status, Pageable pageable);
    
    Page<Incident> findByCategoryId(Long categoryId, Pageable pageable);
    
    @Query("SELECT i FROM Incident i WHERE " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:categoryId IS NULL OR i.category.id = :categoryId) AND " +
           "(:reporterId IS NULL OR i.reporter.id = :reporterId)")
    Page<Incident> findWithFilters(
            @Param("status") IncidentStatus status,
            @Param("categoryId") Long categoryId,
            @Param("reporterId") Long reporterId,
            Pageable pageable);
    
    // Statistics queries
    long countByStatus(IncidentStatus status);
    
    long countByCategoryId(Long categoryId);
    
    @Query("SELECT i.status, COUNT(i) FROM Incident i GROUP BY i.status")
    List<Object[]> countByStatusGrouped();
    
    @Query("SELECT i.category.name, COUNT(i) FROM Incident i GROUP BY i.category.name")
    List<Object[]> countByCategoryGrouped();
    
    @Query("SELECT i.assignedAgent.id, COUNT(i) FROM Incident i WHERE i.assignedAgent IS NOT NULL GROUP BY i.assignedAgent.id")
    List<Object[]> countByAgentGrouped();
}
