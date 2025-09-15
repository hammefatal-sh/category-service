package com.example.categoryservice.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CategoryIdTest {

    @Test
    void 정상적인_값으로_CategoryId_생성() {
        // given
        Long value = 1L;

        // when
        CategoryId categoryId = new CategoryId(value);

        // then
        assertThat(categoryId.getValue()).isEqualTo(value);
    }

    @Test
    void null_값으로_CategoryId_생성시_예외발생() {
        // given & when & then
        assertThatThrownBy(() -> new CategoryId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CategoryId value must be positive");
    }

    @Test
    void 음수_값으로_CategoryId_생성시_예외발생() {
        // given & when & then
        assertThatThrownBy(() -> new CategoryId(-1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CategoryId value must be positive");
    }

    @Test
    void 영_값으로_CategoryId_생성시_예외발생() {
        // given & when & then
        assertThatThrownBy(() -> new CategoryId(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CategoryId value must be positive");
    }

    @Test
    void 같은_값을_가진_CategoryId는_동등하다() {
        // given
        CategoryId categoryId1 = new CategoryId(1L);
        CategoryId categoryId2 = new CategoryId(1L);

        // when & then
        assertThat(categoryId1).isEqualTo(categoryId2);
        assertThat(categoryId1.hashCode()).isEqualTo(categoryId2.hashCode());
    }

    @Test
    void 다른_값을_가진_CategoryId는_동등하지_않다() {
        // given
        CategoryId categoryId1 = new CategoryId(1L);
        CategoryId categoryId2 = new CategoryId(2L);

        // when & then
        assertThat(categoryId1).isNotEqualTo(categoryId2);
        assertThat(categoryId1.hashCode()).isNotEqualTo(categoryId2.hashCode());
    }

    @Test
    void toString_메서드_테스트() {
        // given
        CategoryId categoryId = new CategoryId(1L);

        // when
        String result = categoryId.toString();

        // then
        assertThat(result).contains("CategoryId");
        assertThat(result).contains("1");
    }
}