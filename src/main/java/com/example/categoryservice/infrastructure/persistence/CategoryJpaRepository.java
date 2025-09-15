package com.example.categoryservice.infrastructure.persistence;

import com.example.categoryservice.domain.model.Category;
import com.example.categoryservice.domain.model.CategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryJpaRepository extends JpaRepository<Category, CategoryId> {

    List<Category> findByParentId(CategoryId parentId);

    @Query("SELECT c FROM Category c WHERE c.parentId IS NULL")
    List<Category> findRoots();

    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.parentId = :categoryId")
    boolean hasChildren(@Param("categoryId") CategoryId categoryId);

    @Query("SELECT COALESCE(MAX(c.id.value), 0) FROM Category c")
    Long findMaxId();

    @Query("SELECT COUNT(c) FROM Category c WHERE c.parentId IS NULL")
    long countRootCategories();
}