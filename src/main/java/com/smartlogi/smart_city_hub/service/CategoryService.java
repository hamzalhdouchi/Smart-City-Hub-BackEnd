package com.smartlogi.smart_city_hub.service;

import com.smartlogi.smart_city_hub.dto.request.CreateCategoryRequest;
import com.smartlogi.smart_city_hub.dto.response.CategoryResponse;
import com.smartlogi.smart_city_hub.entity.Category;
import com.smartlogi.smart_city_hub.exception.BadRequestException;
import com.smartlogi.smart_city_hub.exception.ResourceNotFoundException;
import com.smartlogi.smart_city_hub.mapper.CategoryMapper;
import com.smartlogi.smart_city_hub.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    public List<CategoryResponse> getActiveCategories() {
        return categoryRepository.findByActiveTrue().stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        return categoryMapper.toResponse(category);
    }
    
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new BadRequestException("Category with this name already exists");
        }
        
        Category category = categoryMapper.toEntity(request);
        category = categoryRepository.save(category);
        
        log.info("Category created: {}", category.getName());
        return categoryMapper.toResponse(category);
    }
    
    @Transactional
    public CategoryResponse updateCategory(Long id, CreateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        
        // Check for duplicate name (excluding current category)
        categoryRepository.findByName(request.getName())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new BadRequestException("Category with this name already exists");
                    }
                });
        
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIcon(request.getIcon());
        
        category = categoryRepository.save(category);
        log.info("Category updated: {}", category.getName());
        
        return categoryMapper.toResponse(category);
    }
    
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        category.setActive(false);
        categoryRepository.save(category);
        log.info("Category deactivated: {}", category.getName());
    }
}
