package com.example.categoryservice.application.service;

import com.example.categoryservice.application.port.out.CategoryNodeResponse;
import com.example.categoryservice.application.port.out.CategoryResponse;
import com.example.categoryservice.application.port.out.CategoryTreeResponse;
import com.example.categoryservice.domain.model.Category;
import com.example.categoryservice.domain.model.CategoryId;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
            category.getId().getValue(),
            category.getName(),
            category.getDescription(),
            category.getParentId() != null ? category.getParentId().getValue() : null,
            category.getCreatedAt(),
            category.getUpdatedAt()
        );
    }

    public CategoryTreeResponse toCategoryTree(List<Category> categories) {
        return toCategoryTree(categories, null);
    }

    public CategoryTreeResponse toCategoryTree(List<Category> categories, CategoryId rootCategoryId) {
        Map<CategoryId, List<Category>> categoryMap = new HashMap<>();

        // 카테고리를 부모 ID별로 그룹핑 (null 키 허용)
        for (Category category : categories) {
            CategoryId parentId = category.getParentId();
            categoryMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(category);
        }

        List<CategoryNodeResponse> roots;
        if (rootCategoryId == null) {
            // 전체 트리 조회: 루트 카테고리들(parentId가 null)부터 시작
            roots = categoryMap.getOrDefault(null, Collections.emptyList())
                .stream()
                .map(category -> buildCategoryNode(category, categoryMap))
                .collect(Collectors.toList());
        } else {
            // 특정 카테고리를 루트로 하는 트리 조회
            Category rootCategory = categories.stream()
                .filter(c -> c.getId().equals(rootCategoryId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Root category not found: " + rootCategoryId.getValue()));

            roots = List.of(buildCategoryNode(rootCategory, categoryMap));
        }

        return new CategoryTreeResponse(roots);
    }

    private CategoryNodeResponse buildCategoryNode(Category category, Map<CategoryId, List<Category>> categoryMap) {
        List<CategoryNodeResponse> children = categoryMap.getOrDefault(category.getId(), Collections.emptyList())
            .stream()
            .map(child -> buildCategoryNode(child, categoryMap))
            .collect(Collectors.toList());

        return new CategoryNodeResponse(
            category.getId().getValue(),
            category.getName(),
            category.getDescription(),
            category.getCreatedAt(),
            category.getUpdatedAt(),
            children
        );
    }
}