package com.smartlogi.smart_city_hub.mapper;

import com.smartlogi.smart_city_hub.dto.request.CreateCategoryRequest;
import com.smartlogi.smart_city_hub.dto.response.CategoryResponse;
import com.smartlogi.smart_city_hub.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    
    CategoryResponse toResponse(Category category);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    Category toEntity(CreateCategoryRequest request);
}
