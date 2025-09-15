package com.example.categoryservice.testutil;

import com.example.categoryservice.application.port.in.CreateCategoryCommand;
import com.example.categoryservice.application.port.in.UpdateCategoryCommand;
import com.example.categoryservice.application.port.out.CategoryNodeResponse;
import com.example.categoryservice.application.port.out.CategoryResponse;
import com.example.categoryservice.application.port.out.CategoryTreeResponse;
import com.example.categoryservice.domain.model.Category;
import com.example.categoryservice.domain.model.CategoryId;
import com.example.categoryservice.infrastructure.web.CreateCategoryRequest;
import com.example.categoryservice.infrastructure.web.UpdateCategoryRequest;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 테스트용 Category 관련 데이터 생성 빌더
 */
public class CategoryTestDataBuilder {

    public static CategoryBuilder category() {
        return new CategoryBuilder();
    }

    public static CommandBuilder command() {
        return new CommandBuilder();
    }

    public static RequestBuilder request() {
        return new RequestBuilder();
    }

    public static ResponseBuilder response() {
        return new ResponseBuilder();
    }

    // Category 엔티티 빌더
    public static class CategoryBuilder {
        private Long id = 1L;
        private String name = "테스트 카테고리";
        private String description = "테스트 설명";
        private Long parentId = null;

        public CategoryBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public CategoryBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public CategoryBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public CategoryBuilder withParentId(Long parentId) {
            this.parentId = parentId;
            return this;
        }

        public CategoryBuilder asRoot() {
            this.parentId = null;
            return this;
        }

        public Category build() {
            if (parentId == null) {
                return Category.createRoot(new CategoryId(id), name, description);
            } else {
                return Category.create(new CategoryId(id), name, description, new CategoryId(parentId));
            }
        }
    }

    // Command 객체 빌더
    public static class CommandBuilder {
        private String name = "테스트 카테고리";
        private String description = "테스트 설명";
        private Long parentId = null;
        private Long id = 1L;

        public CommandBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public CommandBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public CommandBuilder withParentId(Long parentId) {
            this.parentId = parentId;
            return this;
        }

        public CommandBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public CreateCategoryCommand buildCreate() {
            return new CreateCategoryCommand(name, description, parentId);
        }

        public UpdateCategoryCommand buildUpdate() {
            return new UpdateCategoryCommand(id, name, description, parentId);
        }
    }

    // Request DTO 빌더
    public static class RequestBuilder {
        private String name = "테스트 카테고리";
        private String description = "테스트 설명";
        private Long parentId = null;

        public RequestBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public RequestBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public RequestBuilder withParentId(Long parentId) {
            this.parentId = parentId;
            return this;
        }

        public CreateCategoryRequest buildCreate() {
            return new CreateCategoryRequest(name, description, parentId);
        }

        public UpdateCategoryRequest buildUpdate() {
            return new UpdateCategoryRequest(name, description, parentId);
        }
    }

    // Response DTO 빌더
    public static class ResponseBuilder {
        private Long id = 1L;
        private String name = "테스트 카테고리";
        private String description = "테스트 설명";
        private Long parentId = null;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();

        public ResponseBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public ResponseBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public ResponseBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public ResponseBuilder withParentId(Long parentId) {
            this.parentId = parentId;
            return this;
        }

        public ResponseBuilder withCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ResponseBuilder withUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public CategoryResponse buildResponse() {
            return new CategoryResponse(id, name, description, parentId, createdAt, updatedAt);
        }

        public CategoryNodeResponse buildNodeResponse() {
            return new CategoryNodeResponse(id, name, description, createdAt, updatedAt, List.of());
        }

        public CategoryTreeResponse buildTreeResponse() {
            return new CategoryTreeResponse(List.of(buildNodeResponse()));
        }
    }

    // 미리 정의된 테스트 데이터
    public static class TestData {
        public static final String ELECTRONICS_NAME = "전자제품";
        public static final String ELECTRONICS_DESC = "전자제품 카테고리";
        public static final String SMARTPHONE_NAME = "스마트폰";
        public static final String SMARTPHONE_DESC = "스마트폰 카테고리";
        public static final String BOOK_NAME = "도서";
        public static final String BOOK_DESC = "도서 카테고리";

        public static Category electronicsCategory() {
            return category()
                    .withId(1L)
                    .withName(ELECTRONICS_NAME)
                    .withDescription(ELECTRONICS_DESC)
                    .asRoot()
                    .build();
        }

        public static Category smartphoneCategory() {
            return category()
                    .withId(2L)
                    .withName(SMARTPHONE_NAME)
                    .withDescription(SMARTPHONE_DESC)
                    .withParentId(1L)
                    .build();
        }

        public static Category bookCategory() {
            return category()
                    .withId(3L)
                    .withName(BOOK_NAME)
                    .withDescription(BOOK_DESC)
                    .asRoot()
                    .build();
        }

        public static CreateCategoryCommand createElectronicsCommand() {
            return command()
                    .withName(ELECTRONICS_NAME)
                    .withDescription(ELECTRONICS_DESC)
                    .withParentId(null)
                    .buildCreate();
        }

        public static CreateCategoryRequest createElectronicsRequest() {
            return request()
                    .withName(ELECTRONICS_NAME)
                    .withDescription(ELECTRONICS_DESC)
                    .withParentId(null)
                    .buildCreate();
        }
    }
}