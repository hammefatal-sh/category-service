package com.example.categoryservice.infrastructure.web;

import com.example.categoryservice.application.port.out.CategoryResponse;
import com.example.categoryservice.application.port.out.CategoryTreeResponse;
import com.example.categoryservice.domain.model.Category;
import com.example.categoryservice.domain.model.CategoryId;
import com.example.categoryservice.domain.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@ActiveProfiles("test")
@DisplayName("CategoryController REST API 통합 테스트")
class CategoryControllerRestTemplateTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CategoryRepository categoryRepository;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/v1/categories";
    }

    @Test
    @DisplayName("카테고리 생성 성공")
    void 카테고리_생성_성공() {
        // given
        CreateCategoryRequest request = new CreateCategoryRequest("전자제품", "전자제품 카테고리", null);

        // when
        ResponseEntity<CategoryResponse> response = restTemplate.postForEntity(
                baseUrl(), request, CategoryResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("전자제품");
        assertThat(response.getBody().description()).isEqualTo("전자제품 카테고리");
        assertThat(response.getBody().parentId()).isNull();
    }

    @Test
    @DisplayName("하위 카테고리 생성 성공")
    void 하위_카테고리_생성_성공() {
        // given - 부모 카테고리 먼저 생성
        CreateCategoryRequest parentRequest = new CreateCategoryRequest("전자제품", "전자제품 카테고리", null);
        ResponseEntity<CategoryResponse> parentResponse = restTemplate.postForEntity(
                baseUrl(), parentRequest, CategoryResponse.class);

        Long parentId = parentResponse.getBody().id();
        CreateCategoryRequest childRequest = new CreateCategoryRequest("스마트폰", "스마트폰 카테고리", parentId);

        // when
        ResponseEntity<CategoryResponse> response = restTemplate.postForEntity(
                baseUrl(), childRequest, CategoryResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("스마트폰");
        assertThat(response.getBody().parentId()).isEqualTo(parentId);
    }

    @Test
    @DisplayName("카테고리 생성시 validation 실패")
    void 카테고리_생성시_validation_실패() {
        // given
        CreateCategoryRequest request = new CreateCategoryRequest("", "설명", null);

        // when
        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl(), request, Map.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("카테고리 조회 성공")
    void 카테고리_조회_성공() {
        // given - 카테고리 먼저 생성
        CreateCategoryRequest createRequest = new CreateCategoryRequest("전자제품", "전자제품 카테고리", null);
        ResponseEntity<CategoryResponse> createResponse = restTemplate.postForEntity(
                baseUrl(), createRequest, CategoryResponse.class);
        Long categoryId = createResponse.getBody().id();

        // when
        ResponseEntity<CategoryResponse> response = restTemplate.getForEntity(
                baseUrl() + "/" + categoryId, CategoryResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(categoryId);
        assertThat(response.getBody().name()).isEqualTo("전자제품");
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 조회시 404")
    void 존재하지_않는_카테고리_조회시_404() {
        // when
        ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl() + "/999", Map.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsKey("code");
        assertThat(response.getBody().get("code")).isEqualTo("CATEGORY_NOT_FOUND");
    }

    @Test
    @DisplayName("전체 카테고리 트리 조회")
    void 전체_카테고리_트리_조회() {
        // given - 테스트 데이터 생성
        CreateCategoryRequest request1 = new CreateCategoryRequest("전자제품", "전자제품 카테고리", null);
        CreateCategoryRequest request2 = new CreateCategoryRequest("도서", "도서 카테고리", null);
        restTemplate.postForEntity(baseUrl(), request1, CategoryResponse.class);
        restTemplate.postForEntity(baseUrl(), request2, CategoryResponse.class);

        // when
        ResponseEntity<CategoryTreeResponse> response = restTemplate.getForEntity(
                baseUrl(), CategoryTreeResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().categories()).hasSize(2);
    }

    @Test
    @DisplayName("특정 카테고리 하위 트리 조회")
    void 특정_카테고리_하위_트리_조회() {
        // given - 부모-자식 카테고리 생성
        CreateCategoryRequest parentRequest = new CreateCategoryRequest("전자제품", "전자제품 카테고리", null);
        ResponseEntity<CategoryResponse> parentResponse = restTemplate.postForEntity(
                baseUrl(), parentRequest, CategoryResponse.class);

        Long parentId = parentResponse.getBody().id();
        CreateCategoryRequest childRequest = new CreateCategoryRequest("스마트폰", "스마트폰 카테고리", parentId);
        restTemplate.postForEntity(baseUrl(), childRequest, CategoryResponse.class);

        // when
        ResponseEntity<CategoryTreeResponse> response = restTemplate.getForEntity(
                baseUrl() + "/" + parentId + "/tree", CategoryTreeResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().categories()).hasSize(1);
    }

    @Test
    @DisplayName("카테고리 수정 성공")
    void 카테고리_수정_성공() {
        // given - 카테고리 먼저 생성
        CreateCategoryRequest createRequest = new CreateCategoryRequest("전자제품", "전자제품 카테고리", null);
        ResponseEntity<CategoryResponse> createResponse = restTemplate.postForEntity(
                baseUrl(), createRequest, CategoryResponse.class);
        Long categoryId = createResponse.getBody().id();

        UpdateCategoryRequest updateRequest = new UpdateCategoryRequest("전자기기", "전자기기 카테고리", null);

        // when
        ResponseEntity<CategoryResponse> response = restTemplate.exchange(
                baseUrl() + "/" + categoryId,
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
                CategoryResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("전자기기");
        assertThat(response.getBody().description()).isEqualTo("전자기기 카테고리");
    }

    @Test
    @DisplayName("카테고리 삭제 성공")
    void 카테고리_삭제_성공() {
        // given - 카테고리 먼저 생성
        CreateCategoryRequest createRequest = new CreateCategoryRequest("전자제품", "전자제품 카테고리", null);
        ResponseEntity<CategoryResponse> createResponse = restTemplate.postForEntity(
                baseUrl(), createRequest, CategoryResponse.class);
        Long categoryId = createResponse.getBody().id();

        // when
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl() + "/" + categoryId,
                HttpMethod.DELETE,
                null,
                Void.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("하위 카테고리가 있는 카테고리 삭제시 400")
    void 하위_카테고리가_있는_카테고리_삭제시_400() {
        // given - 부모-자식 카테고리 생성
        CreateCategoryRequest parentRequest = new CreateCategoryRequest("전자제품", "전자제품 카테고리", null);
        ResponseEntity<CategoryResponse> parentResponse = restTemplate.postForEntity(
                baseUrl(), parentRequest, CategoryResponse.class);

        Long parentId = parentResponse.getBody().id();
        CreateCategoryRequest childRequest = new CreateCategoryRequest("스마트폰", "스마트폰 카테고리", parentId);
        restTemplate.postForEntity(baseUrl(), childRequest, CategoryResponse.class);

        // when
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/" + parentId,
                HttpMethod.DELETE,
                null,
                Map.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsKey("code");
        assertThat(response.getBody().get("code")).isEqualTo("CATEGORY_HAS_CHILDREN");
    }
}