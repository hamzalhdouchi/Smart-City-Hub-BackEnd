package com.smartlogi.smart_city_hub.service;

import com.smartlogi.smart_city_hub.dto.request.CreateCategoryRequest;
import com.smartlogi.smart_city_hub.dto.response.CategoryResponse;
import com.smartlogi.smart_city_hub.entity.Category;
import com.smartlogi.smart_city_hub.exception.BadRequestException;
import com.smartlogi.smart_city_hub.exception.ResourceNotFoundException;
import com.smartlogi.smart_city_hub.mapper.CategoryMapper;
import com.smartlogi.smart_city_hub.repository.CategoryRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    private Category mockCategory;
    private CategoryResponse mockCategoryResponse;

    @BeforeEach
    void setUp() {
        mockCategory = Category.builder()
                .id("cat-1")
                .name("Infrastructure")
                .description("Roads, bridges, etc.")
                .icon("road-icon")
                .active(true)
                .build();

        mockCategoryResponse = CategoryResponse.builder()
                .id("cat-1")
                .name("Infrastructure")
                .description("Roads, bridges, etc.")
                .icon("road-icon")
                .active(true)
                .build();
    }

    

    @Nested
    @DisplayName("getAllCategories")
    class GetAllCategoriesTests {

        @Test
        void should_ReturnAllCategories_IncludingInactive() {
            Category inactiveCategory = Category.builder()
                    .id("cat-2")
                    .name("Environment")
                    .active(false)
                    .build();

            when(categoryRepository.findAll()).thenReturn(List.of(mockCategory, inactiveCategory));
            when(categoryMapper.toResponse(mockCategory)).thenReturn(mockCategoryResponse);
            when(categoryMapper.toResponse(inactiveCategory)).thenReturn(new CategoryResponse());

            List<CategoryResponse> result = categoryService.getAllCategories();

            assertEquals(2, result.size());
        }

        @Test
        void should_ReturnEmptyList_When_NoCategoriesExist() {
            when(categoryRepository.findAll()).thenReturn(List.of());

            List<CategoryResponse> result = categoryService.getAllCategories();

            assertTrue(result.isEmpty());
        }
    }

    

    @Nested
    @DisplayName("getActiveCategories")
    class GetActiveCategoriesTests {

        @Test
        void should_ReturnOnlyActiveCategories() {
            when(categoryRepository.findByActiveTrue()).thenReturn(List.of(mockCategory));
            when(categoryMapper.toResponse(mockCategory)).thenReturn(mockCategoryResponse);

            List<CategoryResponse> result = categoryService.getActiveCategories();

            assertEquals(1, result.size());
            assertTrue(result.get(0).getActive());
        }

        @Test
        void should_ReturnEmptyList_When_NoActiveCategories() {
            when(categoryRepository.findByActiveTrue()).thenReturn(List.of());

            List<CategoryResponse> result = categoryService.getActiveCategories();

            assertTrue(result.isEmpty());
        }
    }

    

    @Nested
    @DisplayName("getCategoryById")
    class GetCategoryByIdTests {

        @Test
        void should_ReturnCategory_When_Found() {
            when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(mockCategory));
            when(categoryMapper.toResponse(mockCategory)).thenReturn(mockCategoryResponse);

            CategoryResponse result = categoryService.getCategoryById("cat-1");

            assertNotNull(result);
            assertEquals("cat-1", result.getId());
        }

        @Test
        void should_ThrowNotFound_When_CategoryDoesNotExist() {
            when(categoryRepository.findById("nonexistent")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> categoryService.getCategoryById("nonexistent"));
        }
    }

    

    @Nested
    @DisplayName("createCategory")
    class CreateCategoryTests {

        @Test
        void should_CreateCategory_When_NameIsUnique() {
            CreateCategoryRequest request = new CreateCategoryRequest();
            request.setName("Safety");
            request.setDescription("Public safety issues");

            when(categoryRepository.existsByName("Safety")).thenReturn(false);
            when(categoryMapper.toEntity(request)).thenReturn(mockCategory);
            when(categoryRepository.save(mockCategory)).thenReturn(mockCategory);
            when(categoryMapper.toResponse(mockCategory)).thenReturn(mockCategoryResponse);

            CategoryResponse result = categoryService.createCategory(request);

            assertNotNull(result);
            verify(categoryRepository).save(mockCategory);
        }

        @Test
        void should_ThrowBadRequest_When_NameAlreadyExists() {
            CreateCategoryRequest request = new CreateCategoryRequest();
            request.setName("Infrastructure");

            when(categoryRepository.existsByName("Infrastructure")).thenReturn(true);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> categoryService.createCategory(request));
            assertEquals("Category with this name already exists", ex.getMessage());
            verify(categoryRepository, never()).save(any());
        }
    }

    

    @Nested
    @DisplayName("updateCategory")
    class UpdateCategoryTests {

        @Test
        void should_UpdateCategory_When_ValidRequest() {
            CreateCategoryRequest request = new CreateCategoryRequest();
            request.setName("Updated Infrastructure");
            request.setDescription("Updated desc");
            request.setIcon("new-icon");

            when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(mockCategory));
            when(categoryRepository.findByName("Updated Infrastructure")).thenReturn(Optional.empty());
            when(categoryRepository.save(mockCategory)).thenReturn(mockCategory);
            when(categoryMapper.toResponse(mockCategory)).thenReturn(mockCategoryResponse);

            CategoryResponse result = categoryService.updateCategory("cat-1", request);

            assertNotNull(result);
            assertEquals("Updated Infrastructure", mockCategory.getName());
            assertEquals("Updated desc", mockCategory.getDescription());
            assertEquals("new-icon", mockCategory.getIcon());
        }

        @Test
        void should_ThrowNotFound_When_CategoryDoesNotExist() {
            CreateCategoryRequest request = new CreateCategoryRequest();
            request.setName("Any");

            when(categoryRepository.findById("nonexistent")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> categoryService.updateCategory("nonexistent", request));
        }

        @Test
        void should_ThrowBadRequest_When_NewNameBelongsToAnotherCategory() {
            Category anotherCategory = Category.builder()
                    .id("cat-2")
                    .name("Environment")
                    .build();

            CreateCategoryRequest request = new CreateCategoryRequest();
            request.setName("Environment");

            when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(mockCategory));
            when(categoryRepository.findByName("Environment")).thenReturn(Optional.of(anotherCategory));

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> categoryService.updateCategory("cat-1", request));
            assertEquals("Category with this name already exists", ex.getMessage());
        }

        @Test
        void should_AllowUpdate_When_NameBelongsToSameCategory() {
            CreateCategoryRequest request = new CreateCategoryRequest();
            request.setName("Infrastructure");
            request.setDescription("New description");

            when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(mockCategory));
            when(categoryRepository.findByName("Infrastructure")).thenReturn(Optional.of(mockCategory));
            when(categoryRepository.save(mockCategory)).thenReturn(mockCategory);
            when(categoryMapper.toResponse(mockCategory)).thenReturn(mockCategoryResponse);

            assertDoesNotThrow(() -> categoryService.updateCategory("cat-1", request));
        }
    }

    

    @Nested
    @DisplayName("deleteCategory")
    class DeleteCategoryTests {

        @Test
        void should_SoftDeleteCategory_When_Exists() {
            when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(mockCategory));

            categoryService.deleteCategory("cat-1");

            assertFalse(mockCategory.getActive());
            verify(categoryRepository).save(mockCategory);
        }

        @Test
        void should_ThrowNotFound_When_CategoryDoesNotExist() {
            when(categoryRepository.findById("nonexistent")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> categoryService.deleteCategory("nonexistent"));
        }
    }

    

    @Nested
    @DisplayName("reactivateCategory")
    class ReactivateCategoryTests {

        @Test
        void should_ReactivateCategory_When_Exists() {
            mockCategory.setActive(false);
            when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(mockCategory));
            when(categoryRepository.save(mockCategory)).thenReturn(mockCategory);
            when(categoryMapper.toResponse(mockCategory)).thenReturn(mockCategoryResponse);

            CategoryResponse result = categoryService.reactivateCategory("cat-1");

            assertNotNull(result);
            assertTrue(mockCategory.getActive());
            verify(categoryRepository).save(mockCategory);
        }

        @Test
        void should_ThrowNotFound_When_CategoryDoesNotExist() {
            when(categoryRepository.findById("nonexistent")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> categoryService.reactivateCategory("nonexistent"));
        }
    }
}
