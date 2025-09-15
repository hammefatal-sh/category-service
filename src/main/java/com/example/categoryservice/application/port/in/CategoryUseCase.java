package com.example.categoryservice.application.port.in;

import com.example.categoryservice.application.port.out.CategoryResponse;
import com.example.categoryservice.application.port.out.CategoryTreeResponse;
import com.example.categoryservice.domain.model.CategoryId;

public interface CategoryUseCase {

    /**
     * 새로운 카테고리를 생성합니다.
     */
    CategoryResponse createCategory(CreateCategoryCommand command);

    /**
     * 기존 카테고리를 수정합니다.
     */
    CategoryResponse updateCategory(UpdateCategoryCommand command);

    /**
     * 카테고리를 삭제합니다.
     */
    void deleteCategory(CategoryId categoryId);

    /**
     * 특정 카테고리를 조회합니다.
     */
    CategoryResponse getCategory(CategoryId categoryId);

    /**
     * 모든 카테고리를 트리 구조로 조회합니다.
     */
    CategoryTreeResponse getAllCategories();

    /**
     * 특정 카테고리를 루트로 하는 트리를 조회합니다.
     */
    CategoryTreeResponse getCategoryTree(CategoryId rootCategoryId);
}