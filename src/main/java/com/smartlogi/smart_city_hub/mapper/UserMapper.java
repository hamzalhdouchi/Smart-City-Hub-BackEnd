package com.smartlogi.smart_city_hub.mapper;

import com.smartlogi.smart_city_hub.dto.response.UserResponse;
import com.smartlogi.smart_city_hub.entity.User;
import org.hibernate.Hibernate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "approvedByName", source = "user", qualifiedByName = "getApprovedByName")
    UserResponse toResponse(User user);

    @Named("getApprovedByName")
    default String getApprovedByName(User user) {
        if (user.getApprovedBy() != null && Hibernate.isInitialized(user.getApprovedBy())) {
            return user.getApprovedBy().getFullName();
        }
        return null;
    }
}
