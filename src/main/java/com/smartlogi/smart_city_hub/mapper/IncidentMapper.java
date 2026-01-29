package com.smartlogi.smart_city_hub.mapper;

import com.smartlogi.smart_city_hub.dto.response.IncidentResponse;
import com.smartlogi.smart_city_hub.entity.Incident;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class, CategoryMapper.class, PhotoMapper.class, RatingMapper.class})
public interface IncidentMapper {
    
    @Mapping(target = "commentsCount", expression = "java(incident.getComments() != null ? incident.getComments().size() : 0)")
    IncidentResponse toResponse(Incident incident);
}
