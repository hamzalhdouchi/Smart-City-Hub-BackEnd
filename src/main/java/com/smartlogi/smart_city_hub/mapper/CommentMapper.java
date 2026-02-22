package com.smartlogi.smart_city_hub.mapper;

import com.smartlogi.smart_city_hub.dto.response.CommentResponse;
import com.smartlogi.smart_city_hub.entity.Comment;
import com.smartlogi.smart_city_hub.service.ProfilePhotoService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class CommentMapper {

    @Autowired
    protected ProfilePhotoService profilePhotoService;

    @Mapping(target = "id", source = "id")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorName", source = "author.fullName")
    @Mapping(target = "authorRole", expression = "java(comment.getAuthor().getRole().name())")
    @Mapping(target = "authorPhotoUrl", expression = "java(getAuthorPhotoUrl(comment.getAuthor().getId()))")
    public abstract CommentResponse toResponse(Comment comment);

    @Named("resolvePhotoUrl")
    protected String getAuthorPhotoUrl(String userId) {
        return profilePhotoService.getCurrentProfilePhotoUrl(userId).orElse(null);
    }
}
