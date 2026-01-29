package com.smartlogi.smart_city_hub.mapper;

import com.smartlogi.smart_city_hub.dto.response.RatingResponse;
import com.smartlogi.smart_city_hub.entity.Rating;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RatingMapper {
    
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.fullName")
    RatingResponse toResponse(Rating rating);
}
