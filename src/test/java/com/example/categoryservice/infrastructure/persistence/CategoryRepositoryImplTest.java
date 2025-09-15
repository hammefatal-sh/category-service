package com.example.categoryservice.infrastructure.persistence;

import com.example.categoryservice.domain.model.Category;
import com.example.categoryservice.domain.model.CategoryId;
import com.example.categoryservice.domain.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(CategoryRepositoryImpl.class)
@ActiveProfiles("test")
class CategoryRepositoryImplTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void 카테고리_저장_및_조회() {
        // given
        CategoryId categoryId = new CategoryId(1L);
        Category category = Category.createRoot(categoryId, "전자제품", "전자제품 카테고리");

        // when
        Category savedCategory = categoryRepository.save(category);
        Optional<Category> foundCategory = categoryRepository.findById(categoryId);

        // then
        assertThat(savedCategory).isNotNull();
        assertThat(foundCategory).isPresent();
        assertThat(foundCategory.get().getId()).isEqualTo(categoryId);
        assertThat(foundCategory.get().getName()).isEqualTo("전자제품");
    }

    @Test
    void 루트_카테고리_조회() {
        // given
        Category electronics = Category.createRoot(new CategoryId(1L), "전자제품", "전자제품 카테고리");
        Category books = Category.createRoot(new CategoryId(2L), "도서", "도서 카테고리");
        Category smartphone = Category.create(new CategoryId(3L), "스마트폰", "스마트폰 카테고리", new CategoryId(1L));

        categoryRepository.save(electronics);
        categoryRepository.save(books);
        categoryRepository.save(smartphone);

        // when
        List<Category> rootCategories = categoryRepository.findRoots();

        // then
        assertThat(rootCategories).hasSize(2);
        assertThat(rootCategories)
            .extracting(Category::getName)
            .containsExactlyInAnyOrder("전자제품", "도서");
    }

    @Test
    void 부모_카테고리로_하위_카테고리_조회() {
        // given
        CategoryId parentId = new CategoryId(1L);
        Category parent = Category.createRoot(parentId, "전자제품", "전자제품 카테고리");
        Category child1 = Category.create(new CategoryId(2L), "스마트폰", "스마트폰 카테고리", parentId);
        Category child2 = Category.create(new CategoryId(3L), "노트북", "노트북 카테고리", parentId);

        categoryRepository.save(parent);
        categoryRepository.save(child1);
        categoryRepository.save(child2);

        // when
        List<Category> children = categoryRepository.findByParentId(parentId);

        // then
        assertThat(children).hasSize(2);
        assertThat(children)
            .extracting(Category::getName)
            .containsExactlyInAnyOrder("스마트폰", "노트북");
    }

    @Test
    void 하위_카테고리_존재_확인() {
        // given
        CategoryId parentId = new CategoryId(1L);
        CategoryId childId = new CategoryId(2L);
        Category parent = Category.createRoot(parentId, "전자제품", "전자제품 카테고리");
        Category child = Category.create(childId, "스마트폰", "스마트폰 카테고리", parentId);

        categoryRepository.save(parent);
        categoryRepository.save(child);

        // when & then
        assertThat(categoryRepository.hasChildren(parentId)).isTrue();
        assertThat(categoryRepository.hasChildren(childId)).isFalse();
    }

    @Test
    void 카테고리_삭제() {
        // given
        CategoryId categoryId = new CategoryId(1L);
        Category category = Category.createRoot(categoryId, "전자제품", "전자제품 카테고리");
        categoryRepository.save(category);

        // when
        categoryRepository.deleteById(categoryId);

        // then
        assertThat(categoryRepository.existsById(categoryId)).isFalse();
        assertThat(categoryRepository.findById(categoryId)).isEmpty();
    }

    @Test
    void 다음_ID_생성() {
        // given
        Category category1 = Category.createRoot(new CategoryId(1L), "카테고리1", "설명1");
        Category category2 = Category.createRoot(new CategoryId(3L), "카테고리3", "설명3");
        categoryRepository.save(category1);
        categoryRepository.save(category2);

        // when
        Long nextId = categoryRepository.generateNextId();

        // then
        assertThat(nextId).isEqualTo(4L); // 최대값 3 + 1
    }

    @Test
    void 빈_데이터에서_다음_ID_생성() {
        // when
        Long nextId = categoryRepository.generateNextId();

        // then
        assertThat(nextId).isEqualTo(1L); // 최대값 0 + 1
    }
}