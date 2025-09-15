package com.example.categoryservice.application.service;

import com.example.categoryservice.application.port.in.CategoryUseCase;
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
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService implements CategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponse createCategory(CreateCategoryCommand command) {
        // 1. 부모 카테고리 존재 검증
        CategoryId parentId = null;
        if (command.parentId() != null) {
            parentId = new CategoryId(command.parentId());
            validateParentExists(parentId);
        }

        // 2. 새 ID 생성
        Long newId = categoryRepository.generateNextId();
        CategoryId categoryId = new CategoryId(newId);

        // 3. 카테고리 생성
        Category category = parentId != null
            ? Category.create(categoryId, command.name(), command.description(), parentId)
            : Category.createRoot(categoryId, command.name(), command.description());

        // 4. 저장
        Category savedCategory = categoryRepository.save(category);

        // 5. 캐시 무효화
        evictCaches();

        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    public CategoryResponse updateCategory(UpdateCategoryCommand command) {
        CategoryId categoryId = new CategoryId(command.id());

        // 1. 기존 카테고리 조회
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new CategoryNotFoundException(categoryId));

        // 2. 부모 카테고리 변경 검증
        CategoryId newParentId = null;
        if (command.parentId() != null) {
            newParentId = new CategoryId(command.parentId());
            validateParentChange(categoryId, newParentId);
        }

        // 3. 카테고리 업데이트
        category.updateInfo(command.name(), command.description());
        category.changeParent(newParentId);

        // 4. 저장
        Category savedCategory = categoryRepository.save(category);

        // 5. 캐시 무효화
        evictCaches();

        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    @CacheEvict(value = {"categories", "categoryTree"}, allEntries = true)
    public void deleteCategory(CategoryId categoryId) {
        // 1. 카테고리 존재 검증
        if (!categoryRepository.existsById(categoryId)) {
            throw new CategoryNotFoundException(categoryId);
        }

        // 2. 하위 카테고리 존재 검증
        if (categoryRepository.hasChildren(categoryId)) {
            throw new CategoryHasChildrenException(categoryId);
        }

        // 3. 삭제
        categoryRepository.deleteById(categoryId);
    }

    @Override
    @Cacheable(value = "categories", key = "#categoryId.value")
    public CategoryResponse getCategory(CategoryId categoryId) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new CategoryNotFoundException(categoryId));

        return categoryMapper.toResponse(category);
    }

    @Override
    @Cacheable(value = "categoryTree", key = "'all'")
    public CategoryTreeResponse getAllCategories() {
        List<Category> allCategories = categoryRepository.findAll();
        return categoryMapper.toCategoryTree(allCategories);
    }

    @Override
    @Cacheable(value = "categoryTree", key = "#rootCategoryId.value")
    public CategoryTreeResponse getCategoryTree(CategoryId rootCategoryId) {
        // 루트 카테고리 존재 검증
        if (!categoryRepository.existsById(rootCategoryId)) {
            throw new CategoryNotFoundException(rootCategoryId);
        }

        List<Category> allCategories = categoryRepository.findAll();
        return categoryMapper.toCategoryTree(allCategories, rootCategoryId);
    }

    private void validateParentExists(CategoryId parentId) {
        if (!categoryRepository.existsById(parentId)) {
            throw new CategoryNotFoundException("Parent category not found: " + parentId.getValue());
        }
    }

    private void validateParentChange(CategoryId categoryId, CategoryId newParentId) {
        // 1. 새 부모 카테고리 존재 검증
        validateParentExists(newParentId);

        // 2. 순환 참조 검증
        if (wouldCreateCircularReference(categoryId, newParentId)) {
            throw new CircularReferenceException(categoryId, newParentId);
        }
    }

    private boolean wouldCreateCircularReference(CategoryId categoryId, CategoryId newParentId) {
        CategoryId currentParentId = newParentId;

        while (currentParentId != null) {
            if (currentParentId.equals(categoryId)) {
                return true;
            }

            Category parentCategory = categoryRepository.findById(currentParentId).orElse(null);
            if (parentCategory == null) {
                break;
            }

            currentParentId = parentCategory.getParentId();
        }

        return false;
    }

    @CacheEvict(value = {"categories", "categoryTree"}, allEntries = true)
    private void evictCaches() {
        // 캐시 무효화를 위한 메서드
    }
}