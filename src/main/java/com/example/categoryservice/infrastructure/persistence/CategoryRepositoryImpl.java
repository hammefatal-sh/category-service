package com.example.categoryservice.infrastructure.persistence;

import com.example.categoryservice.domain.model.Category;
import com.example.categoryservice.domain.model.CategoryId;
import com.example.categoryservice.domain.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {

    private final CategoryJpaRepository jpaRepository;

    @Override
    public Category save(Category category) {
        return jpaRepository.save(category);
    }

    @Override
    public Optional<Category> findById(CategoryId id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Category> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<Category> findByParentId(CategoryId parentId) {
        return jpaRepository.findByParentId(parentId);
    }

    @Override
    public List<Category> findRoots() {
        return jpaRepository.findRoots();
    }

    @Override
    public boolean existsById(CategoryId id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public void deleteById(CategoryId id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean hasChildren(CategoryId categoryId) {
        return jpaRepository.hasChildren(categoryId);
    }

    @Override
    public Long generateNextId() {
        Long maxId = jpaRepository.findMaxId();
        return maxId + 1;
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public long countRootCategories() {
        return jpaRepository.countRootCategories();
    }
}