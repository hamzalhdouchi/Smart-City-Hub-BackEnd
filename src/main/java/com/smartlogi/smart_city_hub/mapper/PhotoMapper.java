package com.smartlogi.smart_city_hub.mapper;

import com.smartlogi.smart_city_hub.dto.response.PhotoResponse;
import com.smartlogi.smart_city_hub.entity.IncidentPhoto;
import com.smartlogi.smart_city_hub.service.MinioService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class PhotoMapper {

    @Autowired
    protected MinioService minioService;

    @Mapping(target = "url", expression = "java(minioService.getPresignedUrlSafe(photo.getFilePath()))")
    @Mapping(target = "uploadedById", source = "uploadedBy.id")
    @Mapping(target = "uploadedByName", source = "uploadedBy.fullName")
    @Mapping(target = "createdAt", source = "uploadedAt")
    public abstract PhotoResponse toResponse(IncidentPhoto photo);
}
