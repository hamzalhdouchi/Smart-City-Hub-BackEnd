package com.smartlogi.smart_city_hub.mapper;

import com.smartlogi.smart_city_hub.dto.response.UserResponse;
import com.smartlogi.smart_city_hub.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    UserResponse toResponse(User user);
}
