package com.example.categoryservice.domain.repository;

import com.example.categoryservice.domain.model.Category;
import com.example.categoryservice.domain.model.CategoryId;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {

    /**
     * 카테고리를 저장합니다.
     */
    Category save(Category category);

    /**
     * ID로 카테고리를 조회합니다.
     */
    Optional<Category> findById(CategoryId id);

    /**
     * 모든 카테고리를 조회합니다.
     */
    List<Category> findAll();

    /**
     * 특정 부모 ID를 가진 카테고리들을 조회합니다.
     */
    List<Category> findByParentId(CategoryId parentId);

    /**
     * 루트 카테고리들(parentId가 null인 카테고리들)을 조회합니다.
     */
    List<Category> findRoots();

    /**
     * 카테고리가 존재하는지 확인합니다.
     */
    boolean existsById(CategoryId id);

    /**
     * 카테고리를 삭제합니다.
     */
    void deleteById(CategoryId id);

    /**
     * 특정 카테고리가 하위 카테고리를 가지고 있는지 확인합니다.
     */
    boolean hasChildren(CategoryId categoryId);

    /**
     * 다음 사용 가능한 ID를 생성합니다.
     */
    Long generateNextId();
}