package com.example.categoryservice.testutil;

import com.example.categoryservice.application.port.out.CategoryNodeResponse;
import com.example.categoryservice.application.port.out.CategoryResponse;
import com.example.categoryservice.application.port.out.CategoryTreeResponse;
import com.example.categoryservice.domain.model.Category;
import com.example.categoryservice.domain.model.CategoryId;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Category 관련 테스트 Assertion 유틸리티
 */
public class CategoryAssertions {

    /**
     * Category 엔티티 검증
     */
    public static CategoryAssertion assertThatCategory(Category actual) {
        return new CategoryAssertion(actual);
    }

    /**
     * CategoryResponse 검증
     */
    public static CategoryResponseAssertion assertThatCategoryResponse(CategoryResponse actual) {
        return new CategoryResponseAssertion(actual);
    }

    /**
     * CategoryTreeResponse 검증
     */
    public static CategoryTreeResponseAssertion assertThatCategoryTreeResponse(CategoryTreeResponse actual) {
        return new CategoryTreeResponseAssertion(actual);
    }

    public static class CategoryAssertion {
        private final Category actual;

        public CategoryAssertion(Category actual) {
            this.actual = actual;
        }

        public CategoryAssertion hasId(Long expectedId) {
            assertThat(actual.getId().getValue()).isEqualTo(expectedId);
            return this;
        }

        public CategoryAssertion hasName(String expectedName) {
            assertThat(actual.getName()).isEqualTo(expectedName);
            return this;
        }

        public CategoryAssertion hasDescription(String expectedDescription) {
            assertThat(actual.getDescription()).isEqualTo(expectedDescription);
            return this;
        }

        public CategoryAssertion hasParentId(Long expectedParentId) {
            if (expectedParentId == null) {
                assertThat(actual.getParentId()).isNull();
            } else {
                assertThat(actual.getParentId()).isNotNull();
                assertThat(actual.getParentId().getValue()).isEqualTo(expectedParentId);
            }
            return this;
        }

        public CategoryAssertion isRoot() {
            assertThat(actual.isRoot()).isTrue();
            return this;
        }

        public CategoryAssertion isNotRoot() {
            assertThat(actual.isRoot()).isFalse();
            return this;
        }

        public CategoryAssertion hasCreatedAt() {
            assertThat(actual.getCreatedAt()).isNotNull();
            return this;
        }

        public CategoryAssertion hasUpdatedAt() {
            assertThat(actual.getUpdatedAt()).isNotNull();
            return this;
        }

        public CategoryAssertion isEqualTo(Category expected) {
            assertThat(actual).isEqualTo(expected);
            return this;
        }
    }

    public static class CategoryResponseAssertion {
        private final CategoryResponse actual;

        public CategoryResponseAssertion(CategoryResponse actual) {
            this.actual = actual;
        }

        public CategoryResponseAssertion hasId(Long expectedId) {
            assertThat(actual.id()).isEqualTo(expectedId);
            return this;
        }

        public CategoryResponseAssertion hasName(String expectedName) {
            assertThat(actual.name()).isEqualTo(expectedName);
            return this;
        }

        public CategoryResponseAssertion hasDescription(String expectedDescription) {
            assertThat(actual.description()).isEqualTo(expectedDescription);
            return this;
        }

        public CategoryResponseAssertion hasParentId(Long expectedParentId) {
            assertThat(actual.parentId()).isEqualTo(expectedParentId);
            return this;
        }

        public CategoryResponseAssertion hasNoParent() {
            assertThat(actual.parentId()).isNull();
            return this;
        }

        public CategoryResponseAssertion hasCreatedAt() {
            assertThat(actual.createdAt()).isNotNull();
            return this;
        }

        public CategoryResponseAssertion hasUpdatedAt() {
            assertThat(actual.updatedAt()).isNotNull();
            return this;
        }

        public CategoryResponseAssertion matchesCategory(Category category) {
            hasId(category.getId().getValue());
            hasName(category.getName());
            hasDescription(category.getDescription());
            if (category.getParentId() != null) {
                hasParentId(category.getParentId().getValue());
            } else {
                hasNoParent();
            }
            return this;
        }
    }

    public static class CategoryTreeResponseAssertion {
        private final CategoryTreeResponse actual;

        public CategoryTreeResponseAssertion(CategoryTreeResponse actual) {
            this.actual = actual;
        }

        public CategoryTreeResponseAssertion hasSize(int expectedSize) {
            assertThat(actual.categories()).hasSize(expectedSize);
            return this;
        }

        public CategoryTreeResponseAssertion isEmpty() {
            assertThat(actual.categories()).isEmpty();
            return this;
        }

        public CategoryTreeResponseAssertion isNotEmpty() {
            assertThat(actual.categories()).isNotEmpty();
            return this;
        }

        public CategoryTreeResponseAssertion containsNodeWithName(String expectedName) {
            assertThat(actual.categories())
                    .extracting(CategoryNodeResponse::name)
                    .contains(expectedName);
            return this;
        }

        public CategoryTreeResponseAssertion containsNodeWithId(Long expectedId) {
            assertThat(actual.categories())
                    .extracting(CategoryNodeResponse::id)
                    .contains(expectedId);
            return this;
        }

        public CategoryTreeResponseAssertion hasNodeWithChildren(Long parentId, int expectedChildrenCount) {
            CategoryNodeResponse parentNode = actual.categories().stream()
                    .filter(node -> node.id().equals(parentId))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Node with id " + parentId + " not found"));

            assertThat(parentNode.children()).hasSize(expectedChildrenCount);
            return this;
        }

        public CategoryNodeResponseAssertion firstNode() {
            assertThat(actual.categories()).isNotEmpty();
            return new CategoryNodeResponseAssertion(actual.categories().get(0));
        }
    }

    public static class CategoryNodeResponseAssertion {
        private final CategoryNodeResponse actual;

        public CategoryNodeResponseAssertion(CategoryNodeResponse actual) {
            this.actual = actual;
        }

        public CategoryNodeResponseAssertion hasId(Long expectedId) {
            assertThat(actual.id()).isEqualTo(expectedId);
            return this;
        }

        public CategoryNodeResponseAssertion hasName(String expectedName) {
            assertThat(actual.name()).isEqualTo(expectedName);
            return this;
        }

        public CategoryNodeResponseAssertion hasDescription(String expectedDescription) {
            assertThat(actual.description()).isEqualTo(expectedDescription);
            return this;
        }

        public CategoryNodeResponseAssertion hasChildrenCount(int expectedCount) {
            assertThat(actual.children()).hasSize(expectedCount);
            return this;
        }

        public CategoryNodeResponseAssertion hasNoChildren() {
            assertThat(actual.children()).isEmpty();
            return this;
        }

        public CategoryNodeResponseAssertion hasChildren() {
            assertThat(actual.children()).isNotEmpty();
            return this;
        }
    }

    // 편의 메서드들
    public static void assertCategoriesEqual(List<Category> actual, List<Category> expected) {
        assertThat(actual).hasSize(expected.size());
        for (int i = 0; i < actual.size(); i++) {
            assertThatCategory(actual.get(i))
                    .isEqualTo(expected.get(i));
        }
    }

    public static void assertCategoryResponseMatchesCategory(CategoryResponse response, Category category) {
        assertThatCategoryResponse(response)
                .matchesCategory(category);
    }
}