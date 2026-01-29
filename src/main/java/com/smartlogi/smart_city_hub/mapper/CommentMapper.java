package com.smartlogi.smart_city_hub.mapper;

import com.smartlogi.smart_city_hub.dto.response.CommentResponse;
import com.smartlogi.smart_city_hub.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorName", source = "author.fullName")
    CommentResponse toResponse(Comment comment);
}
