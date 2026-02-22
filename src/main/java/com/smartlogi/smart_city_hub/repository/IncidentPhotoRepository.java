package com.smartlogi.smart_city_hub.repository;

import com.smartlogi.smart_city_hub.entity.IncidentPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentPhotoRepository extends JpaRepository<IncidentPhoto, String> {

    List<IncidentPhoto> findByIncidentIdOrderByUploadedAtDesc(String incidentId);

    void deleteByIncidentId(String incidentId);
}
