package com.example.categoryservice.domain.exception;

import com.example.categoryservice.domain.model.CategoryId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class DomainExceptionTest {

    @Test
    void CategoryNotFoundException_CategoryId로_생성() {
        // given
        CategoryId categoryId = new CategoryId(1L);

        // when
        CategoryNotFoundException exception = new CategoryNotFoundException(categoryId);

        // then
        assertThat(exception.getMessage()).isEqualTo("Category not found: 1");
    }

    @Test
    void CategoryNotFoundException_메시지로_생성() {
        // given
        String message = "Custom error message";

        // when
        CategoryNotFoundException exception = new CategoryNotFoundException(message);

        // then
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void CategoryHasChildrenException_CategoryId로_생성() {
        // given
        CategoryId categoryId = new CategoryId(1L);

        // when
        CategoryHasChildrenException exception = new CategoryHasChildrenException(categoryId);

        // then
        assertThat(exception.getMessage()).isEqualTo("Cannot delete category with children: 1");
    }

    @Test
    void CategoryHasChildrenException_메시지로_생성() {
        // given
        String message = "Custom error message";

        // when
        CategoryHasChildrenException exception = new CategoryHasChildrenException(message);

        // then
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void CircularReferenceException_CategoryId들로_생성() {
        // given
        CategoryId categoryId = new CategoryId(1L);
        CategoryId parentId = new CategoryId(2L);

        // when
        CircularReferenceException exception = new CircularReferenceException(categoryId, parentId);

        // then
        assertThat(exception.getMessage()).isEqualTo("Circular reference detected: 1 -> 2");
    }

    @Test
    void CircularReferenceException_메시지로_생성() {
        // given
        String message = "Custom error message";

        // when
        CircularReferenceException exception = new CircularReferenceException(message);

        // then
        assertThat(exception.getMessage()).isEqualTo(message);
    }
}