package com.example.categoryservice.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CategoryTest {

    @Test
    void 정상적인_루트_카테고리_생성() {
        // given
        CategoryId id = new CategoryId(1L);
        String name = "전자제품";
        String description = "전자제품 카테고리";

        // when
        Category category = Category.createRoot(id, name, description);

        // then
        assertThat(category.getId()).isEqualTo(id);
        assertThat(category.getName()).isEqualTo(name);
        assertThat(category.getDescription()).isEqualTo(description);
        assertThat(category.getParentId()).isNull();
        assertThat(category.isRoot()).isTrue();
    }

    @Test
    void 정상적인_하위_카테고리_생성() {
        // given
        CategoryId id = new CategoryId(2L);
        CategoryId parentId = new CategoryId(1L);
        String name = "스마트폰";
        String description = "스마트폰 카테고리";

        // when
        Category category = Category.create(id, name, description, parentId);

        // then
        assertThat(category.getId()).isEqualTo(id);
        assertThat(category.getName()).isEqualTo(name);
        assertThat(category.getDescription()).isEqualTo(description);
        assertThat(category.getParentId()).isEqualTo(parentId);
        assertThat(category.isRoot()).isFalse();
    }

    @Test
    void 빈_이름으로_카테고리_생성시_예외발생() {
        // given
        CategoryId id = new CategoryId(1L);

        // when & then
        assertThatThrownBy(() -> Category.createRoot(id, "", "설명"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Category name cannot be empty");
    }

    @Test
    void null_이름으로_카테고리_생성시_예외발생() {
        // given
        CategoryId id = new CategoryId(1L);

        // when & then
        assertThatThrownBy(() -> Category.createRoot(id, null, "설명"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Category name cannot be empty");
    }

    @Test
    void 너무_긴_이름으로_카테고리_생성시_예외발생() {
        // given
        CategoryId id = new CategoryId(1L);
        String longName = "a".repeat(101); // 101자

        // when & then
        assertThatThrownBy(() -> Category.createRoot(id, longName, "설명"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Category name cannot exceed 100 characters");
    }

    @Test
    void 카테고리_정보_업데이트() {
        // given
        CategoryId id = new CategoryId(1L);
        Category category = Category.createRoot(id, "전자제품", "전자제품 카테고리");
        String newName = "전자기기";
        String newDescription = "전자기기 카테고리";

        // when
        category.updateInfo(newName, newDescription);

        // then
        assertThat(category.getName()).isEqualTo(newName);
        assertThat(category.getDescription()).isEqualTo(newDescription);
    }

    @Test
    void 카테고리_부모_변경() {
        // given
        CategoryId id = new CategoryId(2L);
        CategoryId originalParentId = new CategoryId(1L);
        CategoryId newParentId = new CategoryId(3L);
        Category category = Category.create(id, "스마트폰", "스마트폰 카테고리", originalParentId);

        // when
        category.changeParent(newParentId);

        // then
        assertThat(category.getParentId()).isEqualTo(newParentId);
        assertThat(category.isRoot()).isFalse();
    }

    @Test
    void 카테고리를_루트로_변경() {
        // given
        CategoryId id = new CategoryId(2L);
        CategoryId parentId = new CategoryId(1L);
        Category category = Category.create(id, "스마트폰", "스마트폰 카테고리", parentId);

        // when
        category.changeParent(null);

        // then
        assertThat(category.getParentId()).isNull();
        assertThat(category.isRoot()).isTrue();
    }

    @Test
    void 자기_자신을_부모로_설정시_예외발생() {
        // given
        CategoryId id = new CategoryId(1L);
        Category category = Category.createRoot(id, "전자제품", "전자제품 카테고리");

        // when & then
        assertThatThrownBy(() -> category.changeParent(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Category cannot be its own parent");
    }

    @Test
    void 같은_ID를_가진_카테고리는_동등하다() {
        // given
        CategoryId id = new CategoryId(1L);
        Category category1 = Category.createRoot(id, "전자제품", "전자제품 카테고리");
        Category category2 = Category.createRoot(id, "다른이름", "다른설명");

        // when & then
        assertThat(category1).isEqualTo(category2);
        assertThat(category1.hashCode()).isEqualTo(category2.hashCode());
    }

    @Test
    void 다른_ID를_가진_카테고리는_동등하지_않다() {
        // given
        CategoryId id1 = new CategoryId(1L);
        CategoryId id2 = new CategoryId(2L);
        Category category1 = Category.createRoot(id1, "전자제품", "전자제품 카테고리");
        Category category2 = Category.createRoot(id2, "전자제품", "전자제품 카테고리");

        // when & then
        assertThat(category1).isNotEqualTo(category2);
        assertThat(category1.hashCode()).isNotEqualTo(category2.hashCode());
    }
}