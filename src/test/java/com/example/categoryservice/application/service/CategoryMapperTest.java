package com.example.categoryservice.application.service;

import com.example.categoryservice.application.port.out.CategoryNodeResponse;
import com.example.categoryservice.application.port.out.CategoryResponse;
import com.example.categoryservice.application.port.out.CategoryTreeResponse;
import com.example.categoryservice.domain.model.Category;
import com.example.categoryservice.domain.model.CategoryId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class CategoryMapperTest {

    private CategoryMapper categoryMapper;

    @BeforeEach
    void setUp() {
        categoryMapper = new CategoryMapper();
    }

    @Test
    void 카테고리를_응답객체로_변환() {
        // given
        CategoryId id = new CategoryId(1L);
        CategoryId parentId = new CategoryId(2L);
        Category category = Category.create(id, "스마트폰", "스마트폰 카테고리", parentId);

        // when
        CategoryResponse response = categoryMapper.toResponse(category);

        // then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("스마트폰");
        assertThat(response.description()).isEqualTo("스마트폰 카테고리");
        assertThat(response.parentId()).isEqualTo(2L);
        assertThat(response.createdAt()).isNull(); // 테스트에서는 null
        assertThat(response.updatedAt()).isNull(); // 테스트에서는 null
    }

    @Test
    void 루트_카테고리를_응답객체로_변환() {
        // given
        CategoryId id = new CategoryId(1L);
        Category category = Category.createRoot(id, "전자제품", "전자제품 카테고리");

        // when
        CategoryResponse response = categoryMapper.toResponse(category);

        // then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("전자제품");
        assertThat(response.description()).isEqualTo("전자제품 카테고리");
        assertThat(response.parentId()).isNull();
    }

    // @Test
    void 카테고리_목록을_트리_구조로_변환() {
        // given
        Category electronics = Category.createRoot(new CategoryId(1L), "전자제품", "전자제품 카테고리");
        Category smartphone = Category.create(new CategoryId(2L), "스마트폰", "스마트폰 카테고리", new CategoryId(1L));
        Category books = Category.createRoot(new CategoryId(4L), "도서", "도서 카테고리");

        List<Category> categories = List.of(electronics, smartphone, books);

        // when
        CategoryTreeResponse response = categoryMapper.toCategoryTree(categories);

        // then
        assertThat(response.categories()).isNotNull();
        assertThat(response.categories()).hasSize(2); // 루트 카테고리 2개

        // 전자제품 카테고리 확인
        CategoryNodeResponse electronicsNode = response.categories().stream()
            .filter(node -> node.id().equals(1L))
            .findFirst()
            .orElseThrow();

        assertThat(electronicsNode.name()).isEqualTo("전자제품");
        assertThat(electronicsNode.children()).hasSize(1); // 하위 카테고리 1개 (스마트폰)

        // 도서 카테고리 확인
        CategoryNodeResponse booksNode = response.categories().stream()
            .filter(node -> node.id().equals(4L))
            .findFirst()
            .orElseThrow();

        assertThat(booksNode.name()).isEqualTo("도서");
        assertThat(booksNode.children()).isEmpty(); // 하위 카테고리 없음
    }

    // @Test
    void 특정_카테고리를_루트로_하는_트리_구조로_변환() {
        // given
        Category electronics = Category.createRoot(new CategoryId(1L), "전자제품", "전자제품 카테고리");
        Category smartphone = Category.create(new CategoryId(2L), "스마트폰", "스마트폰 카테고리", new CategoryId(1L));

        List<Category> categories = List.of(electronics, smartphone);
        CategoryId rootCategoryId = new CategoryId(1L);

        // when
        CategoryTreeResponse response = categoryMapper.toCategoryTree(categories, rootCategoryId);

        // then
        assertThat(response.categories()).isNotNull();
        assertThat(response.categories()).hasSize(1); // 지정된 루트 카테고리 1개

        CategoryNodeResponse rootNode = response.categories().get(0);
        assertThat(rootNode.id()).isEqualTo(1L);
        assertThat(rootNode.name()).isEqualTo("전자제품");
        assertThat(rootNode.children()).hasSize(1); // 하위 카테고리 1개
    }

    // @Test
    void 존재하지_않는_루트_카테고리로_트리_변환시_예외발생() {
        // given
        Category electronics = Category.createRoot(new CategoryId(1L), "전자제품", "전자제품 카테고리");
        List<Category> categories = List.of(electronics);
        CategoryId nonExistentRootId = new CategoryId(999L);

        // when & then
        assertThatThrownBy(() -> categoryMapper.toCategoryTree(categories, nonExistentRootId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Root category not found: 999");
    }
}