package com.example.categoryservice.application.service;

import com.example.categoryservice.application.port.in.CreateCategoryCommand;
import com.example.categoryservice.application.port.in.UpdateCategoryCommand;
import com.example.categoryservice.application.port.out.CategoryResponse;
import com.example.categoryservice.application.port.out.CategoryTreeResponse;
import com.example.categoryservice.domain.exception.CategoryHasChildrenException;
import com.example.categoryservice.domain.exception.CategoryNotFoundException;
import com.example.categoryservice.domain.exception.CircularReferenceException;
import com.example.categoryservice.domain.model.Category;
import com.example.categoryservice.domain.model.CategoryId;
import com.example.categoryservice.domain.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryService(categoryRepository, categoryMapper);
    }

    @Test
    void 루트_카테고리_생성_성공() {
        // given
        CreateCategoryCommand command = new CreateCategoryCommand("전자제품", "전자제품 카테고리", null);
        Long newId = 1L;
        Category savedCategory = Category.createRoot(new CategoryId(newId), "전자제품", "전자제품 카테고리");
        CategoryResponse expectedResponse = new CategoryResponse(1L, "전자제품", "전자제품 카테고리", null, null, null);

        when(categoryRepository.generateNextId()).thenReturn(newId);
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);
        when(categoryMapper.toResponse(savedCategory)).thenReturn(expectedResponse);

        // when
        CategoryResponse result = categoryService.createCategory(command);

        // then
        assertThat(result.name()).isEqualTo("전자제품");
        assertThat(result.description()).isEqualTo("전자제품 카테고리");
        assertThat(result.parentId()).isNull();
        verify(categoryRepository).save(any(Category.class));
        verify(categoryMapper).toResponse(savedCategory);
    }

    @Test
    void 하위_카테고리_생성_성공() {
        // given
        CreateCategoryCommand command = new CreateCategoryCommand("스마트폰", "스마트폰 카테고리", 1L);
        Long newId = 2L;
        CategoryId parentId = new CategoryId(1L);
        Category savedCategory = Category.create(new CategoryId(newId), "스마트폰", "스마트폰 카테고리", parentId);
        CategoryResponse expectedResponse = new CategoryResponse(2L, "스마트폰", "스마트폰 카테고리", 1L, null, null);

        when(categoryRepository.existsById(parentId)).thenReturn(true);
        when(categoryRepository.generateNextId()).thenReturn(newId);
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);
        when(categoryMapper.toResponse(savedCategory)).thenReturn(expectedResponse);

        // when
        CategoryResponse result = categoryService.createCategory(command);

        // then
        assertThat(result.name()).isEqualTo("스마트폰");
        assertThat(result.parentId()).isEqualTo(1L);
        verify(categoryRepository).existsById(parentId);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void 존재하지_않는_부모_카테고리로_생성시_예외발생() {
        // given
        CreateCategoryCommand command = new CreateCategoryCommand("스마트폰", "스마트폰 카테고리", 999L);
        CategoryId parentId = new CategoryId(999L);

        when(categoryRepository.existsById(parentId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> categoryService.createCategory(command))
            .isInstanceOf(CategoryNotFoundException.class);

        verify(categoryRepository).existsById(parentId);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void 카테고리_수정_성공() {
        // given
        UpdateCategoryCommand command = new UpdateCategoryCommand(1L, "전자기기", "전자기기 카테고리", null);
        CategoryId categoryId = new CategoryId(1L);
        Category existingCategory = Category.createRoot(categoryId, "전자제품", "전자제품 카테고리");
        Category updatedCategory = Category.createRoot(categoryId, "전자기기", "전자기기 카테고리");
        CategoryResponse expectedResponse = new CategoryResponse(1L, "전자기기", "전자기기 카테고리", null, null, null);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);
        when(categoryMapper.toResponse(any(Category.class))).thenReturn(expectedResponse);

        // when
        CategoryResponse result = categoryService.updateCategory(command);

        // then
        assertThat(result.name()).isEqualTo("전자기기");
        assertThat(result.description()).isEqualTo("전자기기 카테고리");
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void 존재하지_않는_카테고리_수정시_예외발생() {
        // given
        UpdateCategoryCommand command = new UpdateCategoryCommand(999L, "전자기기", "전자기기 카테고리", null);
        CategoryId categoryId = new CategoryId(999L);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.updateCategory(command))
            .isInstanceOf(CategoryNotFoundException.class);

        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void 순환_참조_생성시_예외발생() {
        // given
        UpdateCategoryCommand command = new UpdateCategoryCommand(1L, "전자제품", "전자제품 카테고리", 2L);
        CategoryId categoryId = new CategoryId(1L);
        CategoryId newParentId = new CategoryId(2L);
        Category existingCategory = Category.createRoot(categoryId, "전자제품", "전자제품 카테고리");
        Category parentCategory = Category.create(new CategoryId(2L), "스마트폰", "스마트폰", categoryId);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.existsById(newParentId)).thenReturn(true);
        when(categoryRepository.findById(newParentId)).thenReturn(Optional.of(parentCategory));

        // when & then
        assertThatThrownBy(() -> categoryService.updateCategory(command))
            .isInstanceOf(CircularReferenceException.class);

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void 카테고리_삭제_성공() {
        // given
        CategoryId categoryId = new CategoryId(1L);

        when(categoryRepository.existsById(categoryId)).thenReturn(true);
        when(categoryRepository.hasChildren(categoryId)).thenReturn(false);

        // when
        categoryService.deleteCategory(categoryId);

        // then
        verify(categoryRepository).existsById(categoryId);
        verify(categoryRepository).hasChildren(categoryId);
        verify(categoryRepository).deleteById(categoryId);
    }

    @Test
    void 존재하지_않는_카테고리_삭제시_예외발생() {
        // given
        CategoryId categoryId = new CategoryId(999L);

        when(categoryRepository.existsById(categoryId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> categoryService.deleteCategory(categoryId))
            .isInstanceOf(CategoryNotFoundException.class);

        verify(categoryRepository).existsById(categoryId);
        verify(categoryRepository, never()).deleteById(any());
    }

    @Test
    void 하위_카테고리가_있는_카테고리_삭제시_예외발생() {
        // given
        CategoryId categoryId = new CategoryId(1L);

        when(categoryRepository.existsById(categoryId)).thenReturn(true);
        when(categoryRepository.hasChildren(categoryId)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> categoryService.deleteCategory(categoryId))
            .isInstanceOf(CategoryHasChildrenException.class);

        verify(categoryRepository).existsById(categoryId);
        verify(categoryRepository).hasChildren(categoryId);
        verify(categoryRepository, never()).deleteById(any());
    }

    @Test
    void 카테고리_조회_성공() {
        // given
        CategoryId categoryId = new CategoryId(1L);
        Category category = Category.createRoot(categoryId, "전자제품", "전자제품 카테고리");
        CategoryResponse expectedResponse = new CategoryResponse(1L, "전자제품", "전자제품 카테고리", null, null, null);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(expectedResponse);

        // when
        CategoryResponse result = categoryService.getCategory(categoryId);

        // then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("전자제품");
        verify(categoryRepository).findById(categoryId);
        verify(categoryMapper).toResponse(category);
    }

    @Test
    void 존재하지_않는_카테고리_조회시_예외발생() {
        // given
        CategoryId categoryId = new CategoryId(999L);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.getCategory(categoryId))
            .isInstanceOf(CategoryNotFoundException.class);

        verify(categoryRepository).findById(categoryId);
        verify(categoryMapper, never()).toResponse(any());
    }

    @Test
    void 전체_카테고리_트리_조회_성공() {
        // given
        List<Category> categories = List.of(
            Category.createRoot(new CategoryId(1L), "전자제품", "전자제품 카테고리")
        );
        CategoryTreeResponse expectedResponse = new CategoryTreeResponse(List.of());

        when(categoryRepository.findAll()).thenReturn(categories);
        when(categoryMapper.toCategoryTree(categories)).thenReturn(expectedResponse);

        // when
        CategoryTreeResponse result = categoryService.getAllCategories();

        // then
        assertThat(result).isEqualTo(expectedResponse);
        verify(categoryRepository).findAll();
        verify(categoryMapper).toCategoryTree(categories);
    }

    @Test
    void 특정_카테고리_트리_조회_성공() {
        // given
        CategoryId rootCategoryId = new CategoryId(1L);
        List<Category> categories = List.of(
            Category.createRoot(rootCategoryId, "전자제품", "전자제품 카테고리")
        );
        CategoryTreeResponse expectedResponse = new CategoryTreeResponse(List.of());

        when(categoryRepository.existsById(rootCategoryId)).thenReturn(true);
        when(categoryRepository.findAll()).thenReturn(categories);
        when(categoryMapper.toCategoryTree(categories, rootCategoryId)).thenReturn(expectedResponse);

        // when
        CategoryTreeResponse result = categoryService.getCategoryTree(rootCategoryId);

        // then
        assertThat(result).isEqualTo(expectedResponse);
        verify(categoryRepository).existsById(rootCategoryId);
        verify(categoryRepository).findAll();
        verify(categoryMapper).toCategoryTree(categories, rootCategoryId);
    }

    @Test
    void 존재하지_않는_루트_카테고리로_트리_조회시_예외발생() {
        // given
        CategoryId rootCategoryId = new CategoryId(999L);

        when(categoryRepository.existsById(rootCategoryId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> categoryService.getCategoryTree(rootCategoryId))
            .isInstanceOf(CategoryNotFoundException.class);

        verify(categoryRepository).existsById(rootCategoryId);
        verify(categoryRepository, never()).findAll();
    }
}