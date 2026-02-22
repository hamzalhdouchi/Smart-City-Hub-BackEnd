package com.smartlogi.smart_city_hub.repository;

import com.smartlogi.smart_city_hub.entity.ProfilePhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfilePhotoRepository extends JpaRepository<ProfilePhoto, String> {
    Optional<ProfilePhoto> findTopByUserIdOrderByUploadedAtDesc(String userId);
}
