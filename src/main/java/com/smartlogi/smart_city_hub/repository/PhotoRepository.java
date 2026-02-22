package com.smartlogi.smart_city_hub.repository;

import com.smartlogi.smart_city_hub.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, String> {

    List<Photo> findByIncidentId(String incidentId);

    void deleteByIncidentId(String incidentId);
}
